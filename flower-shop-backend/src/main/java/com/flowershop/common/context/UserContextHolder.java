package com.flowershop.common.context;

import java.util.Optional;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext userContext) {
        HOLDER.set(userContext);
    }

    public static Optional<UserContext> getOptional() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static UserContext getRequired() {
        return getOptional().orElseThrow(() -> new IllegalStateException("当前线程不存在登录用户上下文"));
    }

    public static Long getUserId() {
        return getRequired().userId();
    }

    public static String getRole() {
        return getRequired().role();
    }

    public static String getUsername() {
        return getRequired().username();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
