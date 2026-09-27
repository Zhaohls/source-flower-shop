package com.flowershop.modules.user.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRoleEnum {

    CUSTOMER("CUSTOMER", "买家"),
    MERCHANT("MERCHANT", "商家"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static UserRoleEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("不支持的用户角色"));
    }
}
