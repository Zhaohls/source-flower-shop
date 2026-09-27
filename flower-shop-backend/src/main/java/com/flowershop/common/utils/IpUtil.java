package com.flowershop.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

public final class IpUtil {

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (StringUtils.isNotBlank(value) && !"unknown".equalsIgnoreCase(value)) {
                return StringUtils.substringBefore(value, ",").trim();
            }
        }
        return request.getRemoteAddr();
    }
}
