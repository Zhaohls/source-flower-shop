package com.flowershop.modules.address.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.address.dto.AddressModels.AddressSaveRequest;
import com.flowershop.modules.address.dto.AddressModels.AddressVO;
import com.flowershop.modules.address.service.AddressAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "买家收货地址")
@RequireRole({"CUSTOMER"})
@RestController
@RequestMapping("/api/customer/addresses")
@RequiredArgsConstructor
public class CustomerAddressController {

    private final AddressAppService addressAppService;

    @Operation(summary = "收货地址列表", description = "返回当前买家的地址列表")
    @GetMapping
    public Result<List<AddressVO>> list() {
        return Result.success(addressAppService.list());
    }

    @Operation(summary = "新增地址", description = "新增当前买家的收货地址")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody AddressSaveRequest request) {
        return Result.success(Map.of("id", addressAppService.create(request)));
    }

    @Operation(summary = "修改地址", description = "修改指定地址信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressSaveRequest request) {
        addressAppService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除地址", description = "删除当前买家的地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressAppService.delete(id);
        return Result.success();
    }

    @Operation(summary = "设置默认地址", description = "设置指定地址为默认地址")
    @PatchMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressAppService.setDefault(id);
        return Result.success();
    }
}
