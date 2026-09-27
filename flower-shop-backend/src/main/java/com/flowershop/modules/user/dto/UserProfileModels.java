package com.flowershop.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class UserProfileModels {

    private UserProfileModels() {
    }

    public record UpdateProfileRequest(
        String nickname,
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String phone,
        @Email(message = "邮箱格式不正确") String email,
        Integer gender
    ) {
    }

    public record ChangePasswordRequest(
        @NotBlank(message = "旧密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 72, message = "新密码长度需要在8到72位之间") String newPassword,
        @NotBlank(message = "确认密码不能为空")
        @Size(max = 72, message = "确认密码不能超过72位") String confirmPassword
    ) {
    }

    public record ProfileResponse(Long userId, String username, String nickname, String avatar, String phone, String email, String role, List<String> roles) {
    }
}
