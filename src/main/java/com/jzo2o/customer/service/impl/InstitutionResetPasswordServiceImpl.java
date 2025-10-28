package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.publics.SmsCodeApi;
import com.jzo2o.api.publics.dto.response.BooleanResDTO;
import com.jzo2o.common.enums.SmsBussinessTypeEnum;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.customer.constants.ServeProviderConstants;
import com.jzo2o.customer.mapper.ServeProviderMapper;
import com.jzo2o.customer.model.domain.ServeProvider;
import com.jzo2o.customer.model.dto.request.InstitutionResetPasswordReqDTO;
import com.jzo2o.customer.service.IInstitutionResetPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
@Service
@Slf4j
public class InstitutionResetPasswordServiceImpl extends ServiceImpl<ServeProviderMapper, ServeProvider> implements IInstitutionResetPasswordService {
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(InstitutionResetPasswordReqDTO institutionResetPasswordReqDTO) {
        String password = institutionResetPasswordReqDTO.getPassword();
        String phone = institutionResetPasswordReqDTO.getPhone();
        String verifyCode = institutionResetPasswordReqDTO.getVerifyCode();
        //非空判断
        if (StringUtils.isBlank(phone)) throw new BadRequestException("手机号不能为空");
        if (StringUtils.isBlank(verifyCode)) throw new BadRequestException("验证码不能为空");
        if (StringUtils.isBlank(password)) throw new BadRequestException("密码不能为空");
        //校验验证码
        BooleanResDTO verify = smsCodeApi.verify(phone, SmsBussinessTypeEnum.INSTITUTION_RESET_PASSWORD, verifyCode);
        if(verify==null||!Boolean.TRUE.equals(verify.getIsSuccess())){
            throw new BadRequestException("验证码错误");
        }
        //校验手机号是否被注册
        ServeProvider serveProvider = lambdaQuery()
                .eq(ServeProvider::getPhone, phone)
                .eq(ServeProvider::getType, ServeProviderConstants.TYPE_INSTITUTION)
                .eq(ServeProvider::getIsDeleted,ServeProviderConstants.IS_DELETED_NO)
                .one();
        if(ObjectUtil.isEmpty(serveProvider)){
            throw new BadRequestException("手机号未注册");
        }
        //设置新密码
        String encodePassword = passwordEncoder.encode(password);
        //更新数据
        boolean update = lambdaUpdate()
                .set(ServeProvider::getPassword, encodePassword)
                .eq(ServeProvider::getId, serveProvider.getId())
                .update();
        if(!update){
            log.info("重置密码失败");
            throw new BadRequestException("重置密码失败");
        }else {
            log.info("重置密码成功");
        }
    }
}
