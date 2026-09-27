package com.flowershop.modules.cart.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.cart.dto.CartModels.AddCartItemRequest;
import com.flowershop.modules.cart.dto.CartModels.CartListVO;
import com.flowershop.modules.cart.dto.CartModels.SelectAllRequest;
import com.flowershop.modules.cart.dto.CartModels.UpdateCartItemRequest;
import com.flowershop.modules.cart.dto.CartModels.UpdateCartSelectionRequest;
import com.flowershop.modules.cart.service.CartAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "买家购物车")
@RequireRole({"CUSTOMER"})
@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CustomerCartController {

    private final CartAppService cartAppService;

    @Operation(summary = "加入购物车", description = "同一商品重复加入时自动合并数量")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody AddCartItemRequest request) {
        cartAppService.add(request.productId(), request.quantity());
        return Result.success();
    }

    @Operation(summary = "购物车列表", description = "返回购物车商品及其有效性状态")
    @GetMapping("/list")
    public Result<CartListVO> list() {
        return Result.success(cartAppService.list());
    }

    @Operation(summary = "修改购物车数量", description = "修改指定购物车项的购买数量")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCartItemRequest request) {
        cartAppService.updateQuantity(id, request.quantity());
        return Result.success();
    }

    @Operation(summary = "删除购物车项", description = "删除当前登录买家的购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartAppService.delete(id);
        return Result.success();
    }

    @Operation(summary = "全选或取消全选", description = "批量设置购物车项的选中状态")
    @PatchMapping("/select-all")
    public Result<Void> selectAll(@Valid @RequestBody SelectAllRequest request) {
        cartAppService.selectAll(request.selected());
        return Result.success();
    }

    @Operation(summary = "修改单个购物车项选中状态", description = "设置当前用户指定购物车项的勾选状态")
    @PatchMapping("/{id}/selected")
    public Result<Void> updateSelection(@PathVariable Long id, @Valid @RequestBody UpdateCartSelectionRequest request) {
        cartAppService.updateSelection(id, request.selected());
        return Result.success();
    }
}
