package com.flowershop.common.annotation;

import com.flowershop.common.constant.LimitTarget;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String keyPrefix();

    int maxCount();

    int windowSeconds();

    LimitTarget target() default LimitTarget.IP;

    String message() default "请求过于频繁，请稍后重试";
}
