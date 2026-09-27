package com.flowershop.modules.common.controller;

import com.flowershop.common.result.Result;
import com.flowershop.common.utils.FileUploadUtil;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.category.service.CategoryService;
import com.flowershop.modules.common.dto.CommonModels.CategoryOption;
import com.flowershop.modules.common.dto.CommonModels.DictItem;
import com.flowershop.modules.common.dto.CommonModels.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "公共模块")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private static final Map<String, List<DictItem>> DICT_DATA = Map.of(
        "wrappingType", List.of(
            new DictItem("韩式包装", "korean"),
            new DictItem("牛皮纸包装", "kraft"),
            new DictItem("精美礼盒", "giftbox")
        ),
        "ribbonColor", List.of(
            new DictItem("红色", "red"),
            new DictItem("粉色", "pink"),
            new DictItem("蓝色", "blue"),
            new DictItem("白色", "white"),
            new DictItem("金色", "gold")
        )
    );

    private final FileUploadUtil fileUploadUtil;
    private final CategoryService categoryService;

    @Operation(summary = "文件上传", description = "支持头像、商品图、贺卡图片等本地上传")
    @PostMapping("/upload")
    public Result<UploadResponse> upload(
        @Parameter(description = "上传文件", required = true) @RequestPart("file") MultipartFile file,
        @Parameter(description = "业务类型：avatar/product/card") @RequestParam(value = "type", required = false) String type
    ) {
        FileUploadUtil.UploadResult uploadResult = fileUploadUtil.saveFile(file, type);
        return Result.success(new UploadResponse(System.currentTimeMillis(), uploadResult.fileName(), uploadResult.fileUrl(), uploadResult.fileSize()));
    }

    @Operation(summary = "获取字典数据", description = "返回前端常用静态字典数据")
    @GetMapping("/dict/{dictType}")
    public Result<List<DictItem>> dict(@PathVariable String dictType) {
        return Result.success(DICT_DATA.getOrDefault(dictType, List.of()));
    }

    @Operation(summary = "获取启用中的商品分类", description = "商品表单和前台首页都可复用")
    @GetMapping("/categories")
    public Result<List<CategoryOption>> categories() {
        List<CategoryOption> categories = categoryService.lambdaQuery()
            .eq(CategoryEntity::getStatus, 1)
            .orderByAsc(CategoryEntity::getSort, CategoryEntity::getId)
            .list()
            .stream()
            .map(item -> new CategoryOption(item.getId(), item.getCategoryName(), item.getCategoryIcon()))
            .toList();
        return Result.success(categories);
    }
}
