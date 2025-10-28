package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.service.IRealNameCertificationManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("operationWorkerCertificationAuditController")
@RequestMapping("/operation/worker-certification-audit")
@Api(tags = "运营端 - 员工认证审核接口")
public class WorkerCertificationAuditController {
    @Resource
    private IRealNameCertificationManageService realNameCertificationManageService;
    @GetMapping("/page")
    @ApiOperation("分页查询员工认证审核信息")
    public PageResult<WorkerCertificationAuditResDTO> getPage(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO){
        return realNameCertificationManageService.getPage(workerCertificationAuditPageQueryReqDTO);
    }
    @PutMapping("/audit/{id}")
    @ApiOperation("审核员工认证信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id",value = "认证申请ID",required = true),
            @ApiImplicitParam(name = "certificationStatus" ,value = "认证状态,2:认证成功,3认证失败",required = true),
            @ApiImplicitParam(name = "rejectReason" ,value = "拒绝原因")
    })
    public void audit(@PathVariable("id") Long id
            ,@RequestParam("certificationStatus")Integer certificationStatus
            ,@RequestParam("rejectReason")String rejectReason){
        realNameCertificationManageService.audit(id,certificationStatus,rejectReason);
    }
}
