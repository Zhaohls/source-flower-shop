package com.flowershop.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class AuthModels {

    private AuthModels() {
    }

    public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50位") String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 72, message = "密码长度需要在8到72位之间") String password,
        @NotBlank(message = "确认密码不能为空")
        @Size(max = 72, message = "确认密码不能超过72位") String confirmPassword,
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone,
        @Email(message = "邮箱格式不正确") String email,
        String nickname,
        @NotBlank(message = "角色不能为空") String role,
        String shopName,
        String shopDesc,
        String shopLogo
    ) {
    }

    public record RegisterResponse(Long userId, String username) {
    }

    public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50位") String username,
        @NotBlank(message = "密码不能为空")
        @Size(max = 72, message = "密码不能超过72位") String password
    ) {
    }

    public record ForgotPasswordRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50位") String username,
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 72, message = "密码长度需要在8到72位之间") String password,
        @NotBlank(message = "确认密码不能为空")
        @Size(max = 72, message = "确认密码不能超过72位") String confirmPassword
    ) {
    }

    public record LoginResponse(String token, Long tokenExpire, UserInfoResponse userInfo) {
    }

    public record UserInfoResponse(
        Long userId,
        String username,
        String nickname,
        String avatar,
        String phone,
        String email,
        String role,
        List<String> roles,
        List<String> permissions,
        String shopName,
        String shopLogo
    ) {
    }
}
