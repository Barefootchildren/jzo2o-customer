package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.constants.RealNameCertificationManageConstants;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.model.domain.AgencyCertificationAudit;
import org.apache.commons.lang3.StringUtils;
import com.jzo2o.customer.mapper.AgencyCertificationMapper;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.mvc.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class AgencyCertificationAuditServiceImpl extends ServiceImpl<AgencyCertificationMapper, AgencyCertification> implements IAgencyCertificationAuditService {
    @Resource
    private AgencyCertificationAuditMapper agencyCertificationAuditMapper;
    /**
     * 添加机构认证审核信息
     *
     * @param agencyCertificationAuditAddReqDTO 机构认证审核添加请求DTO对象，包含认证相关信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNameAuthentication(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO) {
        Long userId = UserContext.currentUserId();
        // 校验用户是否有效
        if (userId == null || userId.equals(0L)) throw new BadRequestException("当前操作用户异常");
        // 校验参数是否为空
        if (ObjectUtil.isEmpty(agencyCertificationAuditAddReqDTO)) throw new BadRequestException("参数不能为空");
        // 校验必填项
        if (StringUtils.isAnyBlank(
                agencyCertificationAuditAddReqDTO.getName(),
                agencyCertificationAuditAddReqDTO.getIdNumber(),
                agencyCertificationAuditAddReqDTO.getLegalPersonName(),
                agencyCertificationAuditAddReqDTO.getLegalPersonIdCardNo(),
                agencyCertificationAuditAddReqDTO.getBusinessLicense())) {
            throw new BadRequestException("必填项不能为空");
        }

        // 校验历史审核状态，防止重复提交
        AgencyCertificationAudit lastAudit = agencyCertificationAuditMapper.selectOne(
                Wrappers.<AgencyCertificationAudit>lambdaQuery()
                        .eq(AgencyCertificationAudit::getServeProviderId, userId)
                        .orderByDesc(AgencyCertificationAudit::getCreateTime)
                        .last("limit 1")
        );
        if (ObjectUtil.isNotEmpty(lastAudit)) {
            if (RealNameCertificationManageConstants.UNAUDITED.equals(lastAudit.getAuditStatus()))
                throw new BadRequestException("该机构已存在待审核的认证信息");
            if (RealNameCertificationManageConstants.AUTHENTICATING.equals(lastAudit.getCertificationStatus()))
                throw new BadRequestException("该机构已存在认证中的认证信息");
            if (RealNameCertificationManageConstants.AUTHENTICATED.equals(lastAudit.getCertificationStatus()))
                throw new BadRequestException("该机构已存在认证成功的认证信息");
        }

        // 设置用户ID并复制属性
        agencyCertificationAuditAddReqDTO.setServeProviderId(userId);
        AgencyCertification agencyCertification = BeanUtils.copyBean(agencyCertificationAuditAddReqDTO, AgencyCertification.class);
        if (ObjectUtil.isEmpty(agencyCertification)) throw new BadRequestException("参数不能为空");
        agencyCertification.setCertificationStatus(RealNameCertificationManageConstants.AUTHENTICATING);
        agencyCertification.setId(userId);

        // 校验主表认证状态，防止覆盖
        AgencyCertification existed = baseMapper.selectById(userId);
        if (existed != null && Objects.equals(existed.getCertificationStatus(), RealNameCertificationManageConstants.AUTHENTICATED)) {
            throw new BadRequestException("该机构已认证，无需重复提交");
        }

        // 保存主表记录
        boolean saved = new LambdaUpdateChainWrapper<>(baseMapper)
                .eq(AgencyCertification::getId, userId)
                .set(AgencyCertification::getName, agencyCertification.getName())
                .set(AgencyCertification::getIdNumber, agencyCertification.getIdNumber())
                .set(AgencyCertification::getLegalPersonName, agencyCertification.getLegalPersonName())
                .set(AgencyCertification::getLegalPersonIdCardNo, agencyCertification.getLegalPersonIdCardNo())
                .set(AgencyCertification::getBusinessLicense, agencyCertification.getBusinessLicense())
                .set(AgencyCertification::getCertificationStatus, agencyCertification.getCertificationStatus())
                .set(AgencyCertification::getUpdateTime, LocalDateTime.now())
                .update();
        if (!saved) {
            // 如果更新失败，尝试插入新记录
            int inserted = baseMapper.insert(agencyCertification);
            if (inserted <= 0) throw new BadRequestException("机构认证信息保存失败");
        }

        // 插入审核记录
        AgencyCertificationAudit newAudit = getAgencyCertificationAudit(userId, agencyCertification);
        int insertedAudit = agencyCertificationAuditMapper.insert(newAudit);
        if (insertedAudit <= 0) throw new BadRequestException("机构实名认证审核信息保存失败");

        log.info("机构实名认证申请提交成功 userId={}", userId);
    }

    /**
     * 创建机构认证审核记录
     *
     * @param userId 服务提供商ID
     * @param agencyCertification 机构认证信息
     * @return 返回新建的机构认证审核对象
     */
    private static AgencyCertificationAudit getAgencyCertificationAudit(Long userId, AgencyCertification agencyCertification) {
        // 创建新的机构认证审核记录
        AgencyCertificationAudit newAudit = new AgencyCertificationAudit();
        newAudit.setServeProviderId(userId);
        newAudit.setName(agencyCertification.getName());
        newAudit.setIdNumber(agencyCertification.getIdNumber());
        newAudit.setLegalPersonName(agencyCertification.getLegalPersonName());
        newAudit.setLegalPersonIdCardNo(agencyCertification.getLegalPersonIdCardNo());
        newAudit.setBusinessLicense(agencyCertification.getBusinessLicense());
        // 设置审核状态为未审核
        newAudit.setAuditStatus(RealNameCertificationManageConstants.UNAUDITED);
        // 设置认证状态为认证中
        newAudit.setCertificationStatus(RealNameCertificationManageConstants.AUTHENTICATING);
        return newAudit;
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

        // 根据用户ID查询拒绝原因信息（取最近一条）
        AgencyCertificationAudit agencyCertificationAudit = agencyCertificationAuditMapper.selectOne(
                Wrappers.<AgencyCertificationAudit>lambdaQuery()
                        .eq(AgencyCertificationAudit::getServeProviderId, userId)
                        .orderByDesc(AgencyCertificationAudit::getCreateTime)
                        .last("limit 1")
        );
        Integer certificationStatus = agencyCertificationAudit.getCertificationStatus();
        if (!RealNameCertificationManageConstants.AUTHENTICATE_FAILED.equals(certificationStatus)) {
            throw new BadRequestException("没有失败记录");
        }
        String rejectReason = agencyCertificationAudit.getRejectReason();
        if (ObjectUtil.isEmpty(rejectReason)) throw new BadRequestException("没有失败记录");
        RejectReasonResDTO rejectReasonResDTO = new RejectReasonResDTO(rejectReason);
        if (ObjectUtil.isEmpty(rejectReasonResDTO)) throw new BadRequestException("没有失败记录");

        // 返回拒绝原因
        return rejectReasonResDTO;
    }

}
