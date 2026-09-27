package com.flowershop.common.interceptor;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.constant.RedisKeyConstants;
import com.flowershop.common.context.UserContext;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.result.ResultCode;
import com.flowershop.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Hidden
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = StringUtils.substringAfter(authorization, "Bearer ").trim();
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception exception) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token 无效或已过期");
        }

        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String username = claims.getSubject();
        String cachedToken = stringRedisTemplate.opsForValue().get(RedisKeyConstants.TOKEN + userId);
        if (StringUtils.isBlank(cachedToken) || !StringUtils.equals(cachedToken, token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }

        checkRole(handlerMethod, role);
        UserContextHolder.set(new UserContext(userId, username, role, token));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private void checkRole(HandlerMethod handlerMethod, String currentRole) {
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return;
        }
        boolean match = Arrays.stream(requireRole.value()).anyMatch(role -> StringUtils.equalsIgnoreCase(role, currentRole));
        if (!match) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
