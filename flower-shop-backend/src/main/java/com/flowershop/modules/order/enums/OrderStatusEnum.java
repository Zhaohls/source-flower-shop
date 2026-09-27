package com.flowershop.modules.order.enums;

import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付待接单"),
    PREPARING(2, "备货中"),
    SHIPPING(3, "配送中"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String description;

    public boolean canCancel() {
        return Set.of(PENDING_PAYMENT.code, PAID.code, PREPARING.code).contains(code);
    }
}
