package com.flowershop.modules.user.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.user.dto.UserProfileModels.ChangePasswordRequest;
import com.flowershop.modules.user.dto.UserProfileModels.ProfileResponse;
import com.flowershop.modules.user.dto.UserProfileModels.UpdateProfileRequest;
import com.flowershop.modules.user.service.UserProfileAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "买家个人中心")
@RequireRole({"CUSTOMER"})
@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final UserProfileAppService userProfileAppService;

    @Operation(summary = "获取个人资料", description = "返回当前登录买家的基础资料")
    @GetMapping
    public Result<ProfileResponse> profile() {
        return Result.success(userProfileAppService.getProfile());
    }

    @Operation(summary = "修改个人信息", description = "修改买家昵称、手机号和邮箱")
    @PutMapping
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        userProfileAppService.updateProfile(request);
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "校验旧密码后更新为新密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileAppService.changePassword(request);
        return Result.success();
    }

    @Operation(summary = "上传头像", description = "上传 JPG/PNG 头像，大小限制 2MB")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return Result.success(Map.of("avatar", userProfileAppService.updateAvatar(file)));
    }
}
