package com.flowershop.common.constant;

public final class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    public static final String TOKEN = "flower:token:";
    public static final String PRODUCT = "flower:product:";
    public static final String HOT_PRODUCT = "flower:product:hot";
    public static final String STOCK_LOCK = "flower:stock:lock:";
    public static final String ORDER_TIMEOUT = "flower:order:timeout:";
    public static final String RATE_LIMIT = "flower:rate-limit:";
}
