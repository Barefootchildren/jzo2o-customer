package com.jzo2o.customer.controller.operation;

import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.AgencyCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.AgencyCertificationAuditResDTO;
import com.jzo2o.customer.service.IRealNameCertificationManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("operationAgencyCertificationAuditController")
@RequestMapping("/operation/agency-certification-audit")
@Api(tags = "运营端 - 机构认证审核相关接口")
public class AgencyCertificationAuditController {
    @Resource
    private IRealNameCertificationManageService realNameCertificationManageService;
    @GetMapping("/page")
    @ApiOperation("分页查询机构认证审核信息")
    public PageResult<AgencyCertificationAuditResDTO> getPage(AgencyCertificationAuditPageQueryReqDTO pageQueryReqDTO){
        return realNameCertificationManageService.getPage(pageQueryReqDTO);
    }
    @PutMapping("/audit/{id}")
    @ApiOperation("审核机构认证信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="id",value = "认证申请ID",required = true),
            @ApiImplicitParam(name = "certificationStatus" ,value = "认证状态,2:认证成功,3认证失败",required = true),
            @ApiImplicitParam(name = "rejectReason" ,value = "拒绝原因")
    })
    public void audit(@PathVariable("id") Long id
            ,@RequestParam("certificationStatus")Integer certificationStatus
            ,@RequestParam("rejectReason")String rejectReason){
        realNameCertificationManageService.agencyAudit(id,certificationStatus,rejectReason);
    }
}
