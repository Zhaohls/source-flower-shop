package com.flowershop.common.utils;

import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockHelper {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
        Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key, String requestId, Duration duration) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, requestId, duration);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(String key, String requestId) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), requestId);
    }
}
