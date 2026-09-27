package com.flowershop.modules.user.service;

import cn.hutool.crypto.digest.BCrypt;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.utils.FileUploadUtil;
import com.flowershop.modules.auth.service.AuthAppService;
import com.flowershop.modules.user.dto.UserProfileModels.ChangePasswordRequest;
import com.flowershop.modules.user.dto.UserProfileModels.ProfileResponse;
import com.flowershop.modules.user.dto.UserProfileModels.UpdateProfileRequest;
import com.flowershop.modules.user.entity.UserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileAppService {

    private final UserService userService;
    private final AuthAppService authAppService;
    private final FileUploadUtil fileUploadUtil;

    public ProfileResponse getProfile() {
        UserEntity userEntity = authAppService.loadCurrentUser();
        return new ProfileResponse(userEntity.getId(), userEntity.getUsername(), userEntity.getNickname(), userEntity.getAvatar(), userEntity.getPhone(), userEntity.getEmail(), userEntity.getRole(), List.of(userEntity.getRole()));
    }

    public void updateProfile(UpdateProfileRequest request) {
        UserEntity userEntity = authAppService.loadCurrentUser();
        userEntity.setNickname(StringUtils.defaultIfBlank(request.nickname(), userEntity.getNickname()));
        userEntity.setPhone(StringUtils.defaultIfBlank(request.phone(), userEntity.getPhone()));
        userEntity.setEmail(StringUtils.defaultIfBlank(request.email(), userEntity.getEmail()));
        userService.updateById(userEntity);
    }

    public void changePassword(ChangePasswordRequest request) {
        if (!StringUtils.equals(request.newPassword(), request.confirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        UserEntity userEntity = authAppService.loadCurrentUser();
        if (!BCrypt.checkpw(request.oldPassword(), userEntity.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        userEntity.setPassword(BCrypt.hashpw(request.newPassword()));
        userService.updateById(userEntity);
    }

    public String updateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("头像文件不能为空");
        }
        if (file.getSize() > 2 * 1024 * 1024L) {
            throw new BusinessException("头像文件大小不能超过2MB");
        }
        String fileName = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase();
        if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            throw new BusinessException("头像仅支持 JPG 或 PNG 格式");
        }
        FileUploadUtil.UploadResult uploadResult = fileUploadUtil.saveFile(file, "avatar");
        UserEntity userEntity = authAppService.loadCurrentUser();
        userEntity.setAvatar(uploadResult.fileUrl());
        userService.updateById(userEntity);
        return uploadResult.fileUrl();
    }
}
