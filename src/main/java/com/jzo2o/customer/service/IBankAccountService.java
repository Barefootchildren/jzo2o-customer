package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;

import java.util.List;

public interface IBankAccountService extends IService<BankAccount> {
    void addBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO);
    BankAccountResDTO addInstitutionBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO);

}
