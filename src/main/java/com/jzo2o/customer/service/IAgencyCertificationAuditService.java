package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.AgencyCertification;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;

public interface IAgencyCertificationAuditService extends IService<AgencyCertification> {
    void addNameAuthentication(AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO);

    RejectReasonResDTO getRejectReason();
}
