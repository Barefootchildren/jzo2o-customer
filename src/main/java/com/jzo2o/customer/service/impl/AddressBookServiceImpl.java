package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.api.publics.MapApi;
import com.jzo2o.api.publics.dto.response.LocationResDTO;
import com.jzo2o.common.expcetions.BadRequestException;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.common.utils.BeanUtils;
import com.jzo2o.common.utils.CollUtils;
import com.jzo2o.common.utils.NumberUtils;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.customer.mapper.AddressBookMapper;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 地址薄 服务实现类
 * </p>
 *
 * @author itcast
 * @since 2023-07-06
 */
@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {

    @Override
    public List<AddressBookResDTO> getByUserIdAndCity(Long userId, String city) {

        List<AddressBook> addressBooks = lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getCity, city)
                .list();
        if(CollUtils.isEmpty(addressBooks)) {
            return new ArrayList<>();
        }
        return BeanUtils.copyToList(addressBooks, AddressBookResDTO.class);
    }
    @Resource
    private MapApi mapApi;
        /**
     * 添加地址簿信息
     * @param reqDTO 地址簿新增请求参数对象，包含地址详细信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddressBook(AddressBookUpsertReqDTO reqDTO) {
        Long userId = UserContext.currentUserId();
        // 参数校验
        if (ObjectUtil.isNull(reqDTO)) {
            log.info("参数错误");
            throw new BadRequestException("参数错误");
        }
        if(userId==0) {
            log.info("用户不存在");
            throw new BadRequestException("用户不存在");
        }
        // 取消默认
        if(reqDTO.getIsDefault()==1){
            cancelDefault();
        }
        // 赋值转换
        AddressBook addressBook = BeanUtil.copyProperties(reqDTO, AddressBook.class);
        // 获取经纬度坐标
        if(StringUtils.isEmpty(reqDTO.getLocation())) {
            String completeAddress = reqDTO.getProvince() +
                    reqDTO.getCity() +
                    reqDTO.getCounty() +
                    reqDTO.getAddress();
            LocationResDTO locationByAddress = mapApi.getLocationByAddress(completeAddress);
            reqDTO.setLocation(locationByAddress.getLocation());
        }
        if(StringUtils.isNotEmpty(reqDTO.getLocation())) {
            addressBook.setLon(NumberUtils.parseDouble(reqDTO.getLocation().split(",")[0]));
            addressBook.setLat(NumberUtils.parseDouble(reqDTO.getLocation().split(",")[1]));
        }
        // 设置用户ID并保存
        addressBook.setUserId(userId);
        boolean save = save(addressBook);
        if(!save) {
            log.info("添加失败");
            throw new BadRequestException("添加失败");
        }
        log.info("添加成功");
    }



    /**
     * 分页查询通讯录列表
     * @param addressBookPageQueryReqDTO 通讯录分页查询参数
     * @return 通讯录分页查询结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddressBookResDTO> getPage(AddressBookPageQueryReqDTO addressBookPageQueryReqDTO) {
        // 参数校验
        if(ObjectUtil.isEmpty(addressBookPageQueryReqDTO))throw new BadRequestException("参数错误");

        // 构造分页查询条件
        Page<AddressBook> page = PageUtils.parsePageQuery(addressBookPageQueryReqDTO, AddressBook.class);
        LambdaQueryWrapper<AddressBook> query = Wrappers.<AddressBook>lambdaQuery().eq(AddressBook::getUserId, UserContext.currentUserId());

        // 执行分页查询
        Page<AddressBook> addressBookPage = baseMapper.selectPage(page, query);

        // 转换分页结果
        return PageUtils.toPage(addressBookPage, AddressBookResDTO.class);
    }

    /**
     * 根据ID查询地址簿信息
     * @param id 地址簿ID，不能为空且不能为0
     * @return AddressBookResDTO 地址簿响应数据传输对象
     * @throws BadRequestException 当参数错误或数据不存在时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressBookResDTO selectById(Long id) {
        // 参数校验
        if(id==0)throw new BadRequestException("参数错误");

        // 查询地址簿信息
        AddressBook addressBook = baseMapper.selectById(id);

        // 数据存在性校验
        if(ObjectUtil.isEmpty(addressBook))throw new BadRequestException("数据不存在");

        // 转换为响应DTO并返回
        return BeanUtils.toBean(addressBook, AddressBookResDTO.class);
    }

    /**
     * 根据ID更新地址簿信息
     * @param id 地址簿ID
     * @param addressBookUpsertReqDTO 地址簿更新请求数据传输对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddressBookById(Long id,AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        Long userId = UserContext.currentUserId();
        // 参数校验
        if (ObjectUtil.isNull(addressBookUpsertReqDTO)) {
            log.info("参数错误");
            throw new BadRequestException("参数错误");
        }
        if(userId==0) {
            log.info("用户不存在");
            throw new BadRequestException("用户不存在");
        }
        // 参数校验
        if(id==0)throw new BadRequestException("参数错误");
        // 取消默认
        if(addressBookUpsertReqDTO.getIsDefault()==1){
            cancelDefault();
        }
        // 查询地址簿信息
        AddressBook addressBook = BeanUtil.copyProperties(addressBookUpsertReqDTO, AddressBook.class);
        addressBook.setId(id);

        // 数据存在性校验
        if(ObjectUtil.isEmpty(addressBook))throw new BadRequestException("数据不存在");
        // 获取经纬度坐标
        if(StringUtils.isEmpty(addressBookUpsertReqDTO.getLocation())) {
            String completeAddress = addressBookUpsertReqDTO.getProvince() +
                    addressBookUpsertReqDTO.getCity() +
                    addressBookUpsertReqDTO.getCounty() +
                    addressBookUpsertReqDTO.getAddress();
            LocationResDTO locationByAddress = mapApi.getLocationByAddress(completeAddress);
            addressBookUpsertReqDTO.setLocation(locationByAddress.getLocation());
        }
        if(StringUtils.isNotEmpty(addressBookUpsertReqDTO.getLocation())) {
            addressBook.setLon(NumberUtils.parseDouble(addressBookUpsertReqDTO.getLocation().split(",")[0]));
            addressBook.setLat(NumberUtils.parseDouble(addressBookUpsertReqDTO.getLocation().split(",")[1]));
        }
        boolean update = updateById(addressBook);
        if(!update) {
            log.info("更新失败");
            throw new BadRequestException("更新失败");
        }
    }

    /**
     * 更新地址簿默认状态
     * @param id 地址簿ID
     * @param flag 默认状态标识
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefault(Long id, Integer flag) {
        // 参数校验
        if (id==0)throw new BadRequestException("参数错误");
        // 取消默认
        if(flag==1){
            cancelDefault();
        }
        // 查询地址簿信息
        LambdaQueryWrapper<AddressBook> query = Wrappers.<AddressBook>lambdaQuery().eq(AddressBook::getId, id);
        AddressBook addressBook = baseMapper.selectOne(query);
        if(ObjectUtil.isEmpty(addressBook))throw new BadRequestException("数据不存在");

        // 更新默认状态
        addressBook.setIsDefault(flag);
        boolean update = updateById(addressBook);
        if(!update) {
            log.info("更新失败");
            throw new BadRequestException("更新失败");
        }
    }

    /**
     * 获取用户的默认地址信息
     *
     * @return AddressBookResDTO 返回用户的默认地址信息DTO对象
     * @throws BadRequestException 当用户没有设置默认地址时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressBookResDTO getDefaultAddress() {
        // 构造查询条件：查找当前用户设置为默认的地址
        LambdaQueryWrapper<AddressBook> queryWrapper = Wrappers.<AddressBook>lambdaQuery()
                .eq(AddressBook::getIsDefault, 1)
                .eq(AddressBook::getUserId, UserContext.currentUserId());
        AddressBook addressBook = baseMapper.selectOne(queryWrapper);

        // 判断查询结果是否存在并返回相应结果
        if(ObjectUtil.isNotEmpty(addressBook)){
            return BeanUtils.toBean(addressBook, AddressBookResDTO.class);
        }else {
            log.info("没有默认地址");
            throw new BadRequestException("没有默认地址");
        }
    }


    /**
     * 取消用户默认地址设置
     *
     * 该方法会查找当前用户设置为默认的地址记录，并将其默认状态取消
     *
     * @param 无参数
     * @return 无返回值
     */
    private void cancelDefault(){
        // 查询当前用户默认地址
        LambdaQueryWrapper<AddressBook> eq = Wrappers.<AddressBook>lambdaQuery()
                .eq(AddressBook::getIsDefault, 1)
                .eq(AddressBook::getUserId, UserContext.currentUserId());
        AddressBook addressBook = baseMapper.selectOne(eq);
        // 取消默认地址设置
        addressBook.setIsDefault(0);
        updateById(addressBook);
    }


}
