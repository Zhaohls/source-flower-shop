package com.flowershop.modules.auth.controller;

import com.flowershop.common.annotation.RateLimit;
import com.flowershop.common.constant.LimitTarget;
import com.flowershop.common.result.Result;
import com.flowershop.modules.auth.dto.AuthModels.ForgotPasswordRequest;
import com.flowershop.modules.auth.dto.AuthModels.LoginRequest;
import com.flowershop.modules.auth.dto.AuthModels.LoginResponse;
import com.flowershop.modules.auth.dto.AuthModels.RegisterRequest;
import com.flowershop.modules.auth.dto.AuthModels.RegisterResponse;
import com.flowershop.modules.auth.dto.AuthModels.UserInfoResponse;
import com.flowershop.modules.auth.service.AuthAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthAppService authAppService;

    @Operation(summary = "用户注册", description = "支持买家和商家注册，商家注册后默认为待审核状态")
    @RateLimit(keyPrefix = "register", maxCount = 3, windowSeconds = 60, target = LimitTarget.IP, message = "注册过于频繁，请稍后再试")
    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authAppService.register(request));
    }

    @Operation(summary = "用户登录", description = "校验用户名密码和账号状态，成功后签发 JWT")
    @RateLimit(keyPrefix = "login", maxCount = 5, windowSeconds = 60, target = LimitTarget.IP, message = "登录尝试过多，请稍后再试")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authAppService.login(request));
    }

    @Operation(summary = "找回密码", description = "通过用户名和手机号重置密码")
    @RateLimit(keyPrefix = "forgot-password", maxCount = 3, windowSeconds = 60, target = LimitTarget.IP, message = "重置密码过于频繁，请稍后再试")
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authAppService.forgotPassword(request);
        return Result.success("密码重置成功", null);
    }

    @Operation(summary = "退出登录", description = "从 Redis 中移除当前用户 Token，使其立即失效")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authAppService.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息", description = "从登录上下文读取用户信息并返回角色与权限")
    @GetMapping("/user-info")
    public Result<UserInfoResponse> userInfo() {
        return Result.success(authAppService.getCurrentUserInfo());
    }
}
