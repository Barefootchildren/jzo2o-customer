package com.jzo2o.customer.controller.agency;

import com.jzo2o.customer.model.dto.request.InstitutionResetPasswordReqDTO;
import com.jzo2o.customer.service.IInstitutionResetPasswordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/agency/serve-provider/institution")
@RestController("innerResetPasswordController")
@Api(tags = "内部接口 - 机构重置密码接口")
public class InnerResetPasswordController {
    @Autowired
    private IInstitutionResetPasswordService iInstitutionResetPasswordService;
    @PostMapping("/resetPassword")
    @ApiOperation("机构重置密码接口")
    public void resetPassword(@RequestBody InstitutionResetPasswordReqDTO institutionResetPasswordReqDTO) {
        iInstitutionResetPasswordService.resetPassword(institutionResetPasswordReqDTO);
    }
}
