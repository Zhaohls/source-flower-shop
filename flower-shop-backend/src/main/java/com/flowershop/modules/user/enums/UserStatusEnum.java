package com.flowershop.modules.user.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "正常"),
    PENDING_AUDIT(2, "待审核"),
    REJECTED(3, "已拒绝");

    private final Integer code;
    private final String description;

    public static UserStatusEnum fromCode(Integer code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("不支持的用户状态"));
    }
}
