package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import org.apache.commons.lang3.StringUtils;
import com.jzo2o.customer.constants.RealNameCertificationManageConstants;
import com.jzo2o.customer.mapper.AgencyCertificationAuditMapper;
import com.jzo2o.customer.mapper.AgencyCertificationMapper;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.mapper.WorkerCertificationMapper;
import com.jzo2o.customer.model.domain.*;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IRealNameCertificationManageService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class RealNameCertificationManageServiceImpl extends ServiceImpl<WorkerCertificationAuditMapper, WorkerCertificationAudit> implements IRealNameCertificationManageService {
    /**
     * 分页查询工人认证审核信息
     *
     * @param workerCertificationAuditPageQueryReqDTO 分页查询条件对象，包含审核状态、认证状态、身份证号、姓名等查询条件
     * @return PageResult<WorkerCertificationAuditResDTO> 分页查询结果，包含工人认证审核信息列表
     * @throws BadRequestException 当查询参数为空时抛出此异常
     */
    @Override
    public PageResult<WorkerCertificationAuditResDTO> getPage(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {
      // 参数校验
      if(ObjectUtil.isEmpty(workerCertificationAuditPageQueryReqDTO))throw new BadRequestException("参数不能为空");

      // 构造分页对象
      Page<WorkerCertificationAudit> workerCertificationAuditPage = PageUtils.parsePageQuery(workerCertificationAuditPageQueryReqDTO, WorkerCertificationAudit.class);

      // 构造查询条件
      // noinspection unchecked
      LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
              .eq(workerCertificationAuditPageQueryReqDTO.getAuditStatus() != null,
                      WorkerCertificationAudit::getAuditStatus,
                      workerCertificationAuditPageQueryReqDTO.getAuditStatus())
              .eq(workerCertificationAuditPageQueryReqDTO.getCertificationStatus() != null,
                      WorkerCertificationAudit::getCertificationStatus,
                      workerCertificationAuditPageQueryReqDTO.getCertificationStatus())
              .eq(StringUtils.isNotBlank(workerCertificationAuditPageQueryReqDTO.getIdCardNo()),
                      WorkerCertificationAudit::getIdCardNo,
                      workerCertificationAuditPageQueryReqDTO.getIdCardNo())
              .like(StringUtils.isNotBlank(workerCertificationAuditPageQueryReqDTO.getName()),
                      WorkerCertificationAudit::getName,
                      workerCertificationAuditPageQueryReqDTO.getName())
              .orderByDesc(WorkerCertificationAudit::getCreateTime);

      // 执行分页查询
      Page<WorkerCertificationAudit> page = baseMapper.selectPage(workerCertificationAuditPage, queryWrapper);
      // 转换并返回结果
      return PageUtils.toPage(page, WorkerCertificationAuditResDTO.class);
    }
    @Resource
    private WorkerCertificationMapper workerCertificationMapper;
    /**
     * 审核工人实名认证信息。
     *
     * @param id 认证记录ID，不能为空或小于等于0
     * @param certificationStatus 认证状态，仅支持“认证通过”和“认证失败”两种状态
     * @param rejectReason 驳回原因，当认证状态为认证失败时必须填写
     * @throws BadRequestException 当参数为空、状态错误、审核重复或数据库更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer certificationStatus, String rejectReason) {
        // 获取当前操作用户的信息
        Long userId = UserContext.currentUserId();
        if (ObjectUtil.isEmpty(userId)) throw new BadRequestException("当前操作人员信息为空");
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        if (ObjectUtil.isEmpty(currentUserInfo)) throw new BadRequestException("当前操作人员信息为空");
        String name = currentUserInfo.getName();
        if (StringUtils.isBlank(name)) throw new BadRequestException("当前操作人员信息为空");

        // 参数校验
        if (id == null || id <= 0) throw new BadRequestException("参数不能为空或参数错误");
        if (certificationStatus == null || certificationStatus == 0) throw new BadRequestException("参数不能为空");
        boolean pass = RealNameCertificationManageConstants.AUTHENTICATED.equals(certificationStatus);
        boolean reject = RealNameCertificationManageConstants.AUTHENTICATE_FAILED.equals(certificationStatus);
        if (!pass && !reject) throw new BadRequestException("参数错误");
        if (reject && StringUtils.isBlank(rejectReason)) throw new BadRequestException("拒绝原因不能为空");

        // 校验记录是否存在且未审核
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
                .eq(WorkerCertificationAudit::getId, id)
                .eq(WorkerCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.UNAUDITED);
        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(workerCertificationAudit)) throw new BadRequestException("认证记录不存在或已审核");

        // 审核更新
        LocalDateTime now = LocalDateTime.now();
        boolean auditUpdated = new LambdaUpdateChainWrapper<>(baseMapper)
                .eq(WorkerCertificationAudit::getId, id)
                .eq(WorkerCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.UNAUDITED)
                .set(WorkerCertificationAudit::getCertificationStatus, certificationStatus)
                .set(WorkerCertificationAudit::getRejectReason, pass ? null : rejectReason)
                .set(WorkerCertificationAudit::getAuditorId, userId)
                .set(WorkerCertificationAudit::getAuditorName, name)
                .set(WorkerCertificationAudit::getAuditTime, now)
                .set(WorkerCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.AUDITED)
                .update();
        if (!auditUpdated) throw new BadRequestException("该记录已审核或不存在");

        // 同步更新主表认证状态
        Long serveProviderId = workerCertificationAudit.getServeProviderId();
        if (ObjectUtil.isEmpty(serveProviderId)) throw new BadRequestException("serveProviderId参数不存在");
        boolean workerCertificationUpdated = new LambdaUpdateChainWrapper<>(workerCertificationMapper)
                .eq(WorkerCertification::getId, serveProviderId)
                .set(WorkerCertification::getCertificationTime, now)
                .set(WorkerCertification::getCertificationStatus, certificationStatus)
                .update();

        if (!workerCertificationUpdated) throw new BadRequestException("服务人员认证信息更新失败");
        log.info("审核成功 id={}, serveProviderId={}, status={}", id, serveProviderId, certificationStatus);
    }

    @Resource
    private AgencyCertificationAuditMapper agencyCertificationAuditMapper;
        /**
     * 分页查询机构认证审核信息
     * @param pageQueryReqDTO 分页查询参数对象，包含审核状态、认证状态、法人姓名、机构名称等查询条件
     * @return PageResult<AgencyCertificationAuditResDTO> 分页查询结果，包含机构认证审核信息列表和分页信息
     */
    @Override
    public PageResult<AgencyCertificationAuditResDTO> getPage(AgencyCertificationAuditPageQueryReqDTO pageQueryReqDTO) {
        // 参数校验
        if(ObjectUtil.isEmpty(pageQueryReqDTO))throw new BadRequestException("参数不能为空");

        // 构造分页对象
        Page<AgencyCertificationAudit> agencyCertificationAuditPage = PageUtils.parsePageQuery(pageQueryReqDTO, AgencyCertificationAudit.class);

        // 构造查询条件
        //noinspection unchecked
        LambdaQueryWrapper<AgencyCertificationAudit> queryWrapper = Wrappers.<AgencyCertificationAudit>lambdaQuery()
                .eq(pageQueryReqDTO.getAuditStatus() != null, AgencyCertificationAudit::getAuditStatus, pageQueryReqDTO.getAuditStatus())
                .eq(pageQueryReqDTO.getCertificationStatus() != null, AgencyCertificationAudit::getCertificationStatus, pageQueryReqDTO.getCertificationStatus())
                .eq(StringUtils.isNotBlank(pageQueryReqDTO.getLegalPersonName()),
                        AgencyCertificationAudit::getLegalPersonName, pageQueryReqDTO.getLegalPersonName())
                .like(StringUtils.isNotBlank(pageQueryReqDTO.getName()),
                        AgencyCertificationAudit::getName, pageQueryReqDTO.getName())
                .orderByDesc(AgencyCertificationAudit::getCreateTime);

        // 执行分页查询
        Page<AgencyCertificationAudit> page = agencyCertificationAuditMapper.selectPage(agencyCertificationAuditPage, queryWrapper);
        // 转换并返回结果
        return PageUtils.toPage(page, AgencyCertificationAuditResDTO.class);
    }
    @Resource
    private AgencyCertificationMapper agencyCertificationMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agencyAudit(Long id, Integer certificationStatus, String rejectReason) {
        // 获取当前操作用户的信息
        Long userId = UserContext.currentUserId();
        if (ObjectUtil.isEmpty(userId)) throw new BadRequestException("当前操作人员信息为空");
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        if (ObjectUtil.isEmpty(currentUserInfo)) throw new BadRequestException("当前操作人员信息为空");
        String name = currentUserInfo.getName();
        if (StringUtils.isBlank(name)) throw new BadRequestException("当前操作人员信息为空");

        // 参数校验
        if (id == null || id <= 0) throw new BadRequestException("参数不能为空或参数错误");
        if (certificationStatus == null || certificationStatus == 0) throw new BadRequestException("参数不能为空");
        boolean pass = RealNameCertificationManageConstants.AUTHENTICATED.equals(certificationStatus);
        boolean reject = RealNameCertificationManageConstants.AUTHENTICATE_FAILED.equals(certificationStatus);
        if (!pass && !reject) throw new BadRequestException("参数错误");
        if (reject && StringUtils.isBlank(rejectReason)) throw new BadRequestException("拒绝原因不能为空");

        // 校验记录是否存在且未审核
        LambdaQueryWrapper<AgencyCertificationAudit> queryWrapper = Wrappers.<AgencyCertificationAudit>lambdaQuery()
                .eq(AgencyCertificationAudit::getId, id)
                .eq(AgencyCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.UNAUDITED);
        AgencyCertificationAudit agencyCertificationAudit = agencyCertificationAuditMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(agencyCertificationAudit)) throw new BadRequestException("认证记录不存在或已审核");

        // 审核更新
        LocalDateTime now = LocalDateTime.now();
        boolean auditUpdated = new LambdaUpdateChainWrapper<>(agencyCertificationAuditMapper)
                .eq(AgencyCertificationAudit::getId, id)
                .eq(AgencyCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.UNAUDITED)
                .set(AgencyCertificationAudit::getCertificationStatus, certificationStatus)
                .set(AgencyCertificationAudit::getRejectReason, pass ? null : rejectReason)
                .set(AgencyCertificationAudit::getAuditorId, userId)
                .set(AgencyCertificationAudit::getAuditorName, name)
                .set(AgencyCertificationAudit::getAuditTime, now)
                .set(AgencyCertificationAudit::getAuditStatus, RealNameCertificationManageConstants.AUDITED)
                .update();
        if (!auditUpdated) throw new BadRequestException("该记录已审核或不存在");

        // 同步更新主表认证状态
        Long serveProviderId = agencyCertificationAudit.getServeProviderId();
        if (ObjectUtil.isEmpty(serveProviderId)) throw new BadRequestException("serveProviderId参数不存在");
        boolean updated = new LambdaUpdateChainWrapper<>(agencyCertificationMapper)
                .eq(AgencyCertification::getId, serveProviderId)
                .set(AgencyCertification::getCertificationTime, now)
                .set(AgencyCertification::getCertificationStatus, certificationStatus)
                .update();

        if (!updated) throw new BadRequestException("服务人员认证信息更新失败");
        log.info("审核成功 id={}, serveProviderId={}, status={}", id, serveProviderId, certificationStatus);
    }


}
