package com.flowershop.modules.common.controller;

import com.flowershop.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统接口")
@RestController
public class SystemController {

    @Operation(summary = "系统首页", description = "用于启动验收和健康检查")
    @GetMapping("/")
    public Result<Map<String, String>> index() {
        return Result.success(Map.of("service", "flower-shop-backend", "status", "UP"));
    }
}
