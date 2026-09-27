package com.flowershop.common.utils;

import org.apache.commons.lang3.StringUtils;

public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskPhone(String phone) {
        if (StringUtils.length(phone) < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
