package com.jzo2o.customer.service;

import com.jzo2o.customer.model.dto.request.InstitutionResetPasswordReqDTO;

public interface IInstitutionResetPasswordService {
    void resetPassword(InstitutionResetPasswordReqDTO institutionResetPasswordReqDTO);
}
