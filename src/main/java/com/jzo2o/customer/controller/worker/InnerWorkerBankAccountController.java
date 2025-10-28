package com.jzo2o.customer.controller.worker;

import cn.hutool.core.bean.BeanUtil;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.IBankAccountService;
import com.jzo2o.mvc.utils.UserContext;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("inner.workerBankAccount")
@RequestMapping("/worker/bank-account")
public class InnerWorkerBankAccountController {
    @Resource
    private IBankAccountService bankAccountService;

    @PostMapping
    @ApiOperation("添加银行卡")
    public void addBankAccount(@RequestBody BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        bankAccountService.addBankAccount(bankAccountUpsertReqDTO);
    }

    @GetMapping("/currentUserBankAccount")
    @ApiOperation("获取当前用户银行账号")
    public BankAccountResDTO queryCurrentUserBankAccount() {
        BankAccount bankAccount = bankAccountService.getById(UserContext.currentUserId());
        return BeanUtil.toBean(bankAccount, BankAccountResDTO.class);
    }

}
