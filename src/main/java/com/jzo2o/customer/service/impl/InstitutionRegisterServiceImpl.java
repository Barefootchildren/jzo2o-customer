package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.publics.SmsCodeApi;
import com.jzo2o.api.publics.dto.response.BooleanResDTO;
import com.jzo2o.common.enums.SmsBussinessTypeEnum;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.utils.IdUtils;
import com.jzo2o.customer.constants.ServeProviderConstants;
import com.jzo2o.customer.mapper.ServeProviderMapper;
import com.jzo2o.customer.model.domain.ServeProvider;
import com.jzo2o.customer.model.dto.request.InstitutionRegisterReqDTO;
import com.jzo2o.customer.service.IInstitutionRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
@Service
@Slf4j
public class InstitutionRegisterServiceImpl extends ServiceImpl<ServeProviderMapper, ServeProvider> implements IInstitutionRegisterService {
    @Resource
    private SmsCodeApi smsCodeApi;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(InstitutionRegisterReqDTO institutionRegisterReqDTO) {
        String verifyCode = institutionRegisterReqDTO.getVerifyCode();
        String password = institutionRegisterReqDTO.getPassword();
        String phone = institutionRegisterReqDTO.getPhone();
        //非空判断
        if (StringUtils.isBlank(phone)) throw new BadRequestException("手机号不能为空");
        if (StringUtils.isBlank(verifyCode)) throw new BadRequestException("验证码不能为空");
        if (StringUtils.isBlank(password)) throw new BadRequestException("密码不能为空");
        //校验验证码
        BooleanResDTO verify = smsCodeApi.verify(phone, SmsBussinessTypeEnum.INSTITION_REGISTER, verifyCode);
        if(!verify.getIsSuccess()){
            throw new BadRequestException("验证码错误");
        }
        //校验手机号是否被注册
        ServeProvider serveProvider = lambdaQuery()
                .eq(ServeProvider::getPhone, phone)
                .eq(ServeProvider::getType, ServeProviderConstants.TYPE_INSTITUTION)
                .eq(ServeProvider::getIsDeleted,ServeProviderConstants.IS_DELETED_NO)
                .one();
        if(ObjectUtil.isNotEmpty(serveProvider)){
            throw new BadRequestException("手机号已被注册");
        }
        //添加数据
        ServeProvider newServeProvider = newServeProvider(phone, password);
        int insert = baseMapper.insert(newServeProvider);
        if(insert <= 0){
            log.info("注册失败");
            throw new BadRequestException("注册失败");
        }else {
            log.info("注册成功");
        }
    }

    private ServeProvider newServeProvider(String phone, String password) {
        ServeProvider serveProvider = new ServeProvider();
        serveProvider.setPhone(phone);
        serveProvider.setType(ServeProviderConstants.TYPE_INSTITUTION);
        serveProvider.setCode(IdUtils.getSnowflakeNextIdStr());
        serveProvider.setName("机构_"+IdUtils.getSnowflakeNextIdStr());
        serveProvider.setStatus(ServeProviderConstants.STATUS_NORMAL);
        String encodePassword = passwordEncoder.encode(password);
        serveProvider.setPassword(encodePassword);
        return serveProvider;
    }
}
