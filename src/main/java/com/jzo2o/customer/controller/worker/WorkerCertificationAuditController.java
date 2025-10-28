package com.jzo2o.customer.controller.worker;

import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/worker/worker-certification-audit")
public class WorkerCertificationAuditController {
    @Resource
    private IWorkerCertificationAuditService workerCertificationAuditService;
    @PostMapping
    @ApiOperation("添加员工认证审核")
    public void addNameAuthentication(@RequestBody WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO){
        workerCertificationAuditService.addNameAuthentication(workerCertificationAuditAddReqDTO);
    }
    @GetMapping("rejectReason")
    @ApiOperation("获取拒绝原因")
    public RejectReasonResDTO getRejectReason(){
        return workerCertificationAuditService.getRejectReason();
    }
}
