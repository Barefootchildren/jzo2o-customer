package com.jzo2o.customer.service;

import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.domain.AddressBook;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 地址薄 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-07-06
 */
public interface IAddressBookService extends IService<AddressBook> {

    /**
     * 根据用户id和城市编码获取地址
     *
     * @param userId 用户id
     * @param cityCode 城市编码
     * @return 地址编码
     */
    List<AddressBookResDTO> getByUserIdAndCity(Long userId, String cityCode);

    void addAddressBook(AddressBookUpsertReqDTO reqDTO);

    PageResult<AddressBookResDTO> getPage(AddressBookPageQueryReqDTO addressBookPageQueryReqDTO);

    AddressBookResDTO selectById(@NotNull(message = "id不能为空") Long id);

    void updateAddressBookById(@NotNull(message = "id不能为空") Long id,AddressBookUpsertReqDTO addressBookUpsertReqDTO);

    void updateDefault(@NotNull(message = "id不能为空") Long id, @NotNull(message = "flag不能为空") Integer flag);

    AddressBookResDTO getDefaultAddress();
}
