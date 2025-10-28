package com.jzo2o.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jzo2o.customer.model.domain.ServeProvider;
import com.jzo2o.customer.model.dto.request.InstitutionRegisterReqDTO;

public interface IInstitutionRegisterService extends IService<ServeProvider>{
    void register(InstitutionRegisterReqDTO institutionRegisterReqDTO);
}
