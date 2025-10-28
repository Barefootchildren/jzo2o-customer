package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;

public interface IWorkerCertificationAuditService extends IService<WorkerCertification> {
    void addNameAuthentication(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO);

    RejectReasonResDTO getRejectReason();
}
