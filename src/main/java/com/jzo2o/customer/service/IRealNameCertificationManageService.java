package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;

public interface IRealNameCertificationManageService extends IService<WorkerCertificationAudit> {
    PageResult<WorkerCertificationAuditResDTO> getPage(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO);

    void audit(Long id, Integer certificationStatus, String rejectReason);
    PageResult<AgencyCertificationAuditResDTO> getPage(AgencyCertificationAuditPageQueryReqDTO pageQueryReqDTO);

    void agencyAudit(Long id, Integer certificationStatus, String rejectReason);
}
