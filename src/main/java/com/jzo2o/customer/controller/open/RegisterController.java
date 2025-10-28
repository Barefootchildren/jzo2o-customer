package com.jzo2o.customer.controller.open;

import com.jzo2o.customer.model.dto.request.InstitutionRegisterReqDTO;
import com.jzo2o.customer.service.IInstitutionRegisterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("openRegisterController")
@RequestMapping("/open/serve-provider/institution")
@Api(tags = "白名单接口 - 客户注册相关接口")
public class RegisterController {
    @Autowired
    private IInstitutionRegisterService iInstitutionRegisterService;
    @PostMapping("/register")
    @ApiOperation("机构注册接口")
    public void register(@RequestBody InstitutionRegisterReqDTO institutionRegisterReqDTO) {
        iInstitutionRegisterService.register(institutionRegisterReqDTO);
    }
}
