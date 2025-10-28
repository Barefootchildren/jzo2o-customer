package com.jzo2o.customer.controller.agency;

import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IAgencyCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/agency/agency-certification-audit")
public class AgencyCertificationAuditController {
    @Resource
    private IAgencyCertificationAuditService agencyCertificationAuditService;
    @PostMapping
    @ApiOperation("添加员工认证审核")
    public void addNameAuthentication(@RequestBody AgencyCertificationAuditAddReqDTO agencyCertificationAuditAddReqDTO){
        agencyCertificationAuditService.addNameAuthentication(agencyCertificationAuditAddReqDTO);
    }
    @GetMapping("rejectReason")
    @ApiOperation("获取拒绝原因")
    public RejectReasonResDTO getRejectReason(){
        return agencyCertificationAuditService.getRejectReason();
    }
}
