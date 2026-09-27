package com.flowershop.modules.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.result.ResultCode;
import com.flowershop.common.utils.JwtUtil;
import com.flowershop.common.utils.MaskUtil;
import com.flowershop.modules.auth.dto.AuthModels.ForgotPasswordRequest;
import com.flowershop.modules.auth.dto.AuthModels.LoginRequest;
import com.flowershop.modules.auth.dto.AuthModels.LoginResponse;
import com.flowershop.modules.auth.dto.AuthModels.RegisterRequest;
import com.flowershop.modules.auth.dto.AuthModels.RegisterResponse;
import com.flowershop.modules.auth.dto.AuthModels.UserInfoResponse;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.enums.UserRoleEnum;
import com.flowershop.modules.user.enums.UserStatusEnum;
import com.flowershop.modules.user.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.flowershop.common.constant.RedisKeyConstants;

@Service
@RequiredArgsConstructor
public class AuthAppService {

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        UserRoleEnum.CUSTOMER.getCode(), List.of("product:view", "cart:manage", "order:create", "order:view", "review:create"),
        UserRoleEnum.MERCHANT.getCode(), List.of("product:manage", "order:manage", "review:reply", "delivery:manage"),
        UserRoleEnum.ADMIN.getCode(), List.of("dashboard:view", "merchant:audit", "product:manage", "order:view", "notice:manage", "banner:manage", "category:manage", "user:manage")
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public RegisterResponse register(RegisterRequest request) {
        if (!StringUtils.equals(request.password(), request.confirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }
        UserRoleEnum roleEnum = UserRoleEnum.fromCode(request.role());
        if (roleEnum == UserRoleEnum.ADMIN) {
            throw new BusinessException("管理员账号不允许自行注册");
        }
        boolean exists = userService.lambdaQuery().eq(UserEntity::getUsername, request.username()).exists();
        if (exists) {
            throw new BusinessException("用户名已存在");
        }
        if (roleEnum == UserRoleEnum.MERCHANT && StringUtils.isBlank(request.shopName())) {
            throw new BusinessException("商家注册必须填写店铺名称");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.username());
        userEntity.setPassword(BCrypt.hashpw(request.password()));
        userEntity.setNickname(StringUtils.defaultIfBlank(request.nickname(), request.username()));
        userEntity.setPhone(request.phone());
        userEntity.setEmail(request.email());
        userEntity.setRole(roleEnum.getCode());
        userEntity.setStatus(roleEnum == UserRoleEnum.MERCHANT ? UserStatusEnum.PENDING_AUDIT.getCode() : UserStatusEnum.ENABLED.getCode());
        userEntity.setShopName(request.shopName());
        userEntity.setShopDesc(request.shopDesc());
        userEntity.setShopLogo(request.shopLogo());
        userService.save(userEntity);
        return new RegisterResponse(userEntity.getId(), userEntity.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity userEntity = userService.getOne(new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getUsername, request.username())
            .last("limit 1"));
        if (userEntity == null || !BCrypt.checkpw(request.password(), userEntity.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (UserStatusEnum.DISABLED.getCode().equals(userEntity.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (UserStatusEnum.PENDING_AUDIT.getCode().equals(userEntity.getStatus())) {
            throw new BusinessException("商家账号待审核，暂不能登录");
        }
        if (UserStatusEnum.REJECTED.getCode().equals(userEntity.getStatus())) {
            throw new BusinessException("账号审核未通过，请联系管理员");
        }

        String token = jwtUtil.generateToken(userEntity.getId(), userEntity.getUsername(), userEntity.getRole());
        stringRedisTemplate.opsForValue().set(RedisKeyConstants.TOKEN + userEntity.getId(), token, jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);
        return new LoginResponse(token, jwtUtil.getExpireSeconds(), buildUserInfo(userEntity));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        if (!StringUtils.equals(request.password(), request.confirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }
        UserEntity userEntity = userService.getOne(new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getUsername, request.username())
            .eq(UserEntity::getPhone, request.phone())
            .last("limit 1"));
        if (userEntity == null) {
            throw new BusinessException("用户名或手机号错误");
        }
        userEntity.setPassword(BCrypt.hashpw(request.password()));
        userService.updateById(userEntity);
        stringRedisTemplate.delete(RedisKeyConstants.TOKEN + userEntity.getId());
    }

    public void logout() {
        stringRedisTemplate.delete(RedisKeyConstants.TOKEN + UserContextHolder.getUserId());
    }

    public UserInfoResponse getCurrentUserInfo() {
        UserEntity userEntity = loadCurrentUser();
        return buildUserInfo(userEntity);
    }

    public UserEntity loadCurrentUser() {
        UserEntity userEntity = userService.getById(UserContextHolder.getUserId());
        if (userEntity == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userEntity;
    }

    private UserInfoResponse buildUserInfo(UserEntity userEntity) {
        List<String> permissions = ROLE_PERMISSIONS.getOrDefault(userEntity.getRole(), List.of());
        return new UserInfoResponse(
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getNickname(),
            userEntity.getAvatar(),
            MaskUtil.maskPhone(userEntity.getPhone()),
            userEntity.getEmail(),
            userEntity.getRole(),
            List.of(userEntity.getRole()),
            permissions,
            userEntity.getShopName(),
            userEntity.getShopLogo()
        );
    }
}
