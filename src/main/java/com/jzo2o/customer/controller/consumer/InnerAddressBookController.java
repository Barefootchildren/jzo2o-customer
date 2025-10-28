package com.jzo2o.customer.controller.consumer;

import com.jzo2o.api.customer.dto.response.AddressBookResDTO;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.model.dto.request.AddressBookPageQueryReqDTO;
import com.jzo2o.customer.model.dto.request.AddressBookUpsertReqDTO;
import com.jzo2o.customer.service.IAddressBookService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("inner.addressBook")
@RequestMapping("consumer/address-book")
public class InnerAddressBookController {
    @Autowired
    private IAddressBookService addressBookService;

    @PostMapping
    @ApiOperation("添加地址")
    public void addressBook(@RequestBody AddressBookUpsertReqDTO addressBookUpsertReqDTO) {
        addressBookService.addAddressBook(addressBookUpsertReqDTO);
    }
    @GetMapping("page")
    @ApiOperation("分页查询地址")
    public PageResult<AddressBookResDTO> getPage( AddressBookPageQueryReqDTO addressBookPageQueryReqDTO){
        return addressBookService.getPage(addressBookPageQueryReqDTO);
    }
    @GetMapping("{id}")
    @ApiOperation("地址簿详情")
    @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataType = "Long", paramType = "path")
    public AddressBookResDTO getById(@NotNull(message = "id不能为空") @PathVariable("id") Long id){
        return addressBookService.selectById(id);
    }
    @PutMapping("{id}")
    @ApiOperation("编辑地址簿")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataType = "Long", paramType = "path")
    })
    public void updateAddressBookById(@NotNull(message = "id不能为空") @PathVariable("id") Long id,@RequestBody AddressBookUpsertReqDTO addressBookUpsertReqDTO){
        addressBookService.updateAddressBookById(id,addressBookUpsertReqDTO);
    }
    @DeleteMapping("batch")
    @ApiOperation("批量删除地址簿")
    @ApiParam(name = "ids", value = "地址簿id列表", required = true)
    public void deleteBatch(@NotNull(message = "id不能为空") @RequestBody List<Long> ids){
        addressBookService.removeByIds(ids);
    }
    @PutMapping("default")
    @ApiOperation("设置默认地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "地址簿id", required = true, dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name="flag",value = "是否为默认地址，0默认，1非默认",readOnly = true,dataType = "Integer",paramType = "query")
    })
    public void setDefault(@NotNull(message = "id不能为空")@RequestParam Long id,
                           @NotNull(message = "flag不能为空")@RequestParam Integer flag){
        addressBookService.updateDefault(id,flag);
    }
    @GetMapping("defaultAddress")
    @ApiOperation("查询默认地址")
    public AddressBookResDTO getDefaultAddress(){
        return addressBookService.getDefaultAddress();
    }
}
