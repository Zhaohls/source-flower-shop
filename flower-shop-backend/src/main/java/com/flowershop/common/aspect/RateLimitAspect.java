package com.flowershop.common.aspect;

import com.flowershop.common.annotation.RateLimit;
import com.flowershop.common.constant.LimitTarget;
import com.flowershop.common.constant.RedisKeyConstants;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.result.ResultCode;
import com.flowershop.common.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String identity = resolveIdentity(rateLimit.target());
        long now = System.currentTimeMillis();
        String key = RedisKeyConstants.RATE_LIMIT + rateLimit.keyPrefix() + ":" + identity;
        long minScore = now - rateLimit.windowSeconds() * 1000L;

        stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, minScore);
        Long count = stringRedisTemplate.opsForZSet().zCard(key);
        if (count != null && count >= rateLimit.maxCount()) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, rateLimit.message());
        }
        stringRedisTemplate.opsForZSet().add(key, now + ":" + UUID.randomUUID(), now);
        stringRedisTemplate.expire(key, rateLimit.windowSeconds() + 5L, TimeUnit.SECONDS);
        return joinPoint.proceed();
    }

    private String resolveIdentity(LimitTarget limitTarget) {
        if (limitTarget == LimitTarget.USER && UserContextHolder.getOptional().isPresent()) {
            return String.valueOf(UserContextHolder.getUserId());
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes, "请求上下文不存在").getRequest();
        return IpUtil.getClientIp(request);
    }
}
