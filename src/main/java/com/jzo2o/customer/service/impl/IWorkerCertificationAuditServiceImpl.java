package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import com.jzo2o.customer.constants.RealNameCertificationManageConstants;
import com.jzo2o.customer.constants.WorkerCertificationConstants;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.mvc.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Slf4j
public class IWorkerCertificationAuditServiceImpl extends ServiceImpl<WorkerCertificationMapper, WorkerCertification> implements IWorkerCertificationAuditService {
    @Resource
    private WorkerCertificationAuditMapper workerCertificationAuditMapper;
    /**
     * 工人实名认证申请
     *
     * @param workerCertificationAuditAddReqDTO  工人实名认证申请信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNameAuthentication(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        Long userId = UserContext.currentUserId();
        // 校验用户是否有效
        if (userId == null || userId.equals(0L)) throw new BadRequestException("当前操作用户异常");
        // 校验参数是否为空
        if (ObjectUtil.isEmpty(workerCertificationAuditAddReqDTO)) throw new BadRequestException("参数不能为空");
        // 校验必填项
        if (StringUtils.isAnyBlank(workerCertificationAuditAddReqDTO.getName(),
                workerCertificationAuditAddReqDTO.getIdCardNo(),
                workerCertificationAuditAddReqDTO.getFrontImg(),
                workerCertificationAuditAddReqDTO.getBackImg(),
                workerCertificationAuditAddReqDTO.getCertificationMaterial())) {
            throw new BadRequestException("必填项不能为空");
        }

        // 设置用户ID并复制属性
        workerCertificationAuditAddReqDTO.setServeProviderId(userId);
        WorkerCertification workerCertification = BeanUtils.copyBean(workerCertificationAuditAddReqDTO, WorkerCertification.class);
        if (ObjectUtil.isEmpty(workerCertification)) throw new BadRequestException("参数不能为空");
        workerCertification.setCertificationStatus(WorkerCertificationConstants.AUTHENTICATING);

        Long serveProviderId = workerCertificationAuditAddReqDTO.getServeProviderId();
        if (ObjectUtil.isEmpty(serveProviderId) || serveProviderId <= 0) throw new BadRequestException("serveProviderId参数不存在");
        workerCertification.setId(serveProviderId);

        // 校验历史审核状态，防止重复提交
        WorkerCertificationAudit workerCertificationAudit = workerCertificationAuditMapper.selectOne(
                Wrappers.<WorkerCertificationAudit>lambdaQuery()
                        .eq(WorkerCertificationAudit::getServeProviderId, serveProviderId)
                        .orderByDesc(WorkerCertificationAudit::getCreateTime)
                        .last("limit 1")
        );
        if (ObjectUtil.isNotEmpty(workerCertificationAudit)) {
            if (RealNameCertificationManageConstants.UNAUDITED.equals(workerCertificationAudit.getAuditStatus()))
                throw new BadRequestException("该用户已存在待审核的实名认证信息");
            if (RealNameCertificationManageConstants.AUTHENTICATING.equals(workerCertificationAudit.getCertificationStatus()))
                throw new BadRequestException("该用户已存在认证中的实名认证信息");
            if (RealNameCertificationManageConstants.AUTHENTICATED.equals(workerCertificationAudit.getCertificationStatus()))
                throw new BadRequestException("该用户已存在认证成功的实名认证信息");
        }

        // 校验主表认证状态，防止覆盖
        WorkerCertification existed = getById(serveProviderId);
        if (existed != null && Objects.equals(existed.getCertificationStatus(), WorkerCertificationConstants.AUTHENTICATED)) {
            throw new BadRequestException("该用户已认证，无需重复提交");
        }

        // 保存主表记录
        boolean save = saveOrUpdate(workerCertification);
        if (!save) throw new BadRequestException("服务人员认证信息保存失败");

        // 插入审核记录
        WorkerCertificationAudit newWorkerCertificationAudit = getWorkerCertificationAudit(workerCertification);
        int inserted = workerCertificationAuditMapper.insert(newWorkerCertificationAudit);
        if (inserted <= 0) throw new BadRequestException("服务人员实名认证审核信息保存失败");
    }

    /**
     * 构建审核记录对象
     * 根据工人认证信息生成对应的审核表数据，用于插入 worker_certification_audit。
     */
    private static WorkerCertificationAudit getWorkerCertificationAudit(WorkerCertification workerCertification) {
        WorkerCertificationAudit workerCertificationAudit = new WorkerCertificationAudit();
        workerCertificationAudit.setServeProviderId(workerCertification.getId());
        workerCertificationAudit.setName(workerCertification.getName());
        workerCertificationAudit.setIdCardNo(workerCertification.getIdCardNo());
        workerCertificationAudit.setFrontImg(workerCertification.getFrontImg());
        workerCertificationAudit.setBackImg(workerCertification.getBackImg());
        workerCertificationAudit.setCertificationMaterial(workerCertification.getCertificationMaterial());
        workerCertificationAudit.setAuditStatus(RealNameCertificationManageConstants.UNAUDITED);
        workerCertificationAudit.setCertificationStatus(RealNameCertificationManageConstants.AUTHENTICATING);
        return workerCertificationAudit;
    }

    /**
     * 获取拒绝原因信息
     *
     * @return RejectReasonResDTO 拒绝原因响应数据传输对象
     * @throws BadRequestException 当用户ID为空、用户不存在或拒绝原因为空时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RejectReasonResDTO getRejectReason() {
        // 获取当前用户ID
        Long userId = UserContext.currentUserId();
        if (userId == null || userId <= 0) throw new BadRequestException("用户ID不能为空");

        // 根据用户ID查询拒绝原因信息
        WorkerCertificationAudit workerCertificationAudit = workerCertificationAuditMapper.selectOne(
                Wrappers.<WorkerCertificationAudit>lambdaQuery()
                        .eq(WorkerCertificationAudit::getServeProviderId, userId)
                        .orderByDesc(WorkerCertificationAudit::getCreateTime)
                        .last("limit 1")
        );
        Integer certificationStatus = workerCertificationAudit.getCertificationStatus();
        if(!RealNameCertificationManageConstants.AUTHENTICATE_FAILED.equals(certificationStatus)){
            throw new BadRequestException("没有失败记录");
        }
        String rejectReason = workerCertificationAudit.getRejectReason();
        if (ObjectUtil.isEmpty(rejectReason)) throw new BadRequestException("没有失败记录");
        RejectReasonResDTO rejectReasonResDTO = new RejectReasonResDTO(rejectReason);
        if (ObjectUtil.isEmpty(rejectReasonResDTO)) throw new BadRequestException("没有失败记录");

        // 返回拒绝原因
        return rejectReasonResDTO;
    }
}
