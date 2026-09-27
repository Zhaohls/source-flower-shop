package com.flowershop.modules.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayTypeEnum {

    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    MOCK(3, "模拟支付");

    private final Integer code;
    private final String description;
}
