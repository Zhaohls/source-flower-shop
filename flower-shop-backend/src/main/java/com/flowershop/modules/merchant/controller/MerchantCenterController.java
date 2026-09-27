package com.flowershop.modules.merchant.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.model.PageQuery;
import com.flowershop.common.result.Result;
import com.flowershop.modules.merchant.dto.MerchantModels.DeliveryItemVO;
import com.flowershop.modules.merchant.dto.MerchantModels.InventoryAdjustRequest;
import com.flowershop.modules.merchant.dto.MerchantModels.InventoryItemVO;
import com.flowershop.modules.merchant.dto.MerchantModels.PageResult;
import com.flowershop.modules.merchant.dto.MerchantModels.SalesStatisticsVO;
import com.flowershop.modules.merchant.dto.MerchantModels.ShopInfoUpdateRequest;
import com.flowershop.modules.merchant.dto.MerchantModels.ShopInfoVO;
import com.flowershop.modules.merchant.service.MerchantCenterAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商家后台扩展")
@RequireRole({"MERCHANT"})
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantCenterController {

    private final MerchantCenterAppService merchantCenterAppService;

    @Operation(summary = "库存列表", description = "分页查看当前商家的库存，并支持低库存筛选")
    @GetMapping("/inventory")
    public Result<PageResult<InventoryItemVO>> inventory(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Boolean lowStock
    ) {
        return Result.success(merchantCenterAppService.inventory(new PageQuery(pageNum, pageSize), lowStock));
    }

    @Operation(summary = "调整库存", description = "商家手动增加或减少指定商品库存")
    @PostMapping("/inventory/adjust")
    public Result<Void> adjustInventory(@Valid @RequestBody InventoryAdjustRequest request) {
        merchantCenterAppService.adjustInventory(request);
        return Result.success();
    }

    @Operation(summary = "配送列表", description = "分页查看当前商家的配送单")
    @GetMapping("/delivery")
    public Result<PageResult<DeliveryItemVO>> delivery(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status
    ) {
        return Result.success(merchantCenterAppService.delivery(new PageQuery(pageNum, pageSize), status));
    }

    @Operation(summary = "销售统计", description = "按时间范围返回销售额、订单数、分类占比和热销商品")
    @GetMapping("/statistics/sales")
    public Result<SalesStatisticsVO> salesStatistics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.success(merchantCenterAppService.salesStatistics(startDate, endDate));
    }

    @Operation(summary = "店铺信息", description = "读取当前商家的店铺资料")
    @GetMapping("/shop-info")
    public Result<ShopInfoVO> shopInfo() {
        return Result.success(merchantCenterAppService.shopInfo());
    }

    @Operation(summary = "更新店铺信息", description = "保存当前商家的店铺名称、简介、Logo 和联系电话")
    @PutMapping("/shop-info")
    public Result<Void> updateShopInfo(@Valid @RequestBody ShopInfoUpdateRequest request) {
        merchantCenterAppService.updateShopInfo(request);
        return Result.success();
    }
}
