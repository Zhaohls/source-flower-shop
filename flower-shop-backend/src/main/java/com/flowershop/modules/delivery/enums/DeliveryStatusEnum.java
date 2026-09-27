package com.flowershop.modules.delivery.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatusEnum {

    PENDING(0, "待配送"),
    SHIPPING(1, "配送中"),
    DELIVERED(2, "已送达");

    private final Integer code;
    private final String description;
}
