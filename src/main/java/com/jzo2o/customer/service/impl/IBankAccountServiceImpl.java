package com.jzo2o.customer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectById;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.customer.mapper.BankAccountMapper;
import com.jzo2o.customer.model.domain.BankAccount;
import com.jzo2o.customer.model.dto.request.BankAccountUpsertReqDTO;
import com.jzo2o.customer.model.dto.response.BankAccountResDTO;
import com.jzo2o.customer.service.IBankAccountService;
import com.jzo2o.mvc.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IBankAccountServiceImpl extends ServiceImpl<BankAccountMapper, BankAccount> implements IBankAccountService {
        /**
     * 添加银行卡账户
     *
     * @param bankAccountUpsertReqDTO 银行卡账户信息传输对象，包含要添加的银行卡账户的详细信息
     * @throws BadRequestException 当参数为空或添加银行卡失败时抛出此异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        // 参数校验
        if(ObjectUtil.isEmpty(bankAccountUpsertReqDTO))throw new BadRequestException("参数不能为空");

        // 转换DTO对象为实体对象并插入数据库
        BankAccount bankAccount = BeanUtils.copyBean(bankAccountUpsertReqDTO, BankAccount.class);
        bankAccount.setId(UserContext.currentUserId());
        int insert = baseMapper.insert(bankAccount);

        // 检查插入结果并记录日志
        if(insert <= 0){
            log.info("添加银行卡失败");
            throw new BadRequestException("添加银行卡失败");
        }else {
            log.info("添加银行卡成功");
        }
    }

    @Override
    public BankAccountResDTO addInstitutionBankAccount(BankAccountUpsertReqDTO bankAccountUpsertReqDTO) {
        // 参数校验
        if(ObjectUtil.isEmpty(bankAccountUpsertReqDTO))throw new BadRequestException("参数不能为空");

        // 转换DTO对象为实体对象并插入数据库
        BankAccount bankAccount = BeanUtils.copyBean(bankAccountUpsertReqDTO, BankAccount.class);
        bankAccount.setId(UserContext.currentUserId());
        int insert = baseMapper.insert(bankAccount);

        // 检查插入结果并记录日志
        if(insert <= 0){
            log.info("添加银行卡失败");
            throw new BadRequestException("添加银行卡失败");
        }else {
            BankAccount selectById = baseMapper.selectById(bankAccount.getId());
            log.info("添加银行卡成功");
            return BeanUtils.copyBean(selectById, BankAccountResDTO.class);
        }
    }


}
