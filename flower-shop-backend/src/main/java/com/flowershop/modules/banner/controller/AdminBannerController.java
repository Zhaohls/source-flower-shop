package com.flowershop.modules.banner.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.admin.dto.AdminModels.StatusRequest;
import com.flowershop.modules.banner.dto.BannerModels.BannerSaveRequest;
import com.flowershop.modules.banner.dto.BannerModels.BannerVO;
import com.flowershop.modules.banner.dto.BannerModels.PageResult;
import com.flowershop.modules.banner.service.BannerAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员轮播图管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerAppService bannerAppService;

    @Operation(summary = "轮播图列表", description = "分页查看首页轮播图")
    @GetMapping
    public Result<PageResult<BannerVO>> list(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bannerAppService.list(new com.flowershop.common.model.PageQuery(pageNum, pageSize)));
    }

    @Operation(summary = "新增轮播图", description = "创建新的首页轮播图")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody BannerSaveRequest request) {
        return Result.success(Map.of("id", bannerAppService.create(request)));
    }

    @Operation(summary = "修改轮播图", description = "更新轮播图内容")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BannerSaveRequest request) {
        bannerAppService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除轮播图", description = "删除指定轮播图")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerAppService.delete(id);
        return Result.success();
    }

    @Operation(summary = "切换轮播图状态", description = "快速启用或停用轮播图")
    @PatchMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        bannerAppService.updateStatus(id, request.status());
        return Result.success();
    }
}
