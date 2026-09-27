package com.flowershop.common.aspect;

import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.result.Result;
import com.flowershop.common.result.ResultCode;
import com.flowershop.common.utils.IpUtil;
import com.flowershop.modules.admin.entity.OperationLogEntity;
import com.flowershop.modules.admin.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        HttpServletRequest request = currentRequest();
        if (request == null || !shouldLog(request, result) || UserContextHolder.getOptional().isEmpty()) {
            return result;
        }

        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            OperationLogEntity entity = new OperationLogEntity();
            entity.setOperatorId(UserContextHolder.getUserId());
            entity.setOperator(UserContextHolder.getUsername());
            entity.setRole(UserContextHolder.getRole());
            entity.setModule(resolveModule(request.getRequestURI()));
            entity.setAction(resolveAction(request, method));
            entity.setRequestMethod(request.getMethod());
            entity.setRequestUri(request.getRequestURI());
            entity.setIp(IpUtil.getClientIp(request));
            operationLogService.save(entity);
        } catch (Exception exception) {
            log.error("记录操作日志失败 uri={}", request.getRequestURI(), exception);
        }
        return result;
    }

    private boolean shouldLog(HttpServletRequest request, Object result) {
        if (!(result instanceof Result<?> apiResult) || ResultCode.SUCCESS.getCode() != apiResult.getCode()) {
            return false;
        }
        String requestUri = request.getRequestURI();
        if (!StringUtils.startsWithAny(requestUri, "/api/admin", "/api/merchant")) {
            return false;
        }
        if (StringUtils.startsWith(requestUri, "/api/admin/system")) {
            return false;
        }

        String method = request.getMethod();
        if (StringUtils.equalsAnyIgnoreCase(method, "POST", "PUT", "PATCH", "DELETE")) {
            return true;
        }
        if (!StringUtils.equalsIgnoreCase(method, "GET")) {
            return false;
        }

        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern instanceof String value && StringUtils.contains(value, "{");
    }

    private String resolveModule(String requestUri) {
        String[] segments = StringUtils.split(requestUri, '/');
        if (segments == null || segments.length < 3) {
            return "系统管理";
        }
        String moduleKey = segments[2];
        return switch (moduleKey) {
            case "products" -> "商品管理";
            case "orders" -> "订单管理";
            case "users" -> "用户管理";
            case "merchants" -> "商家管理";
            case "categories" -> "分类管理";
            case "notices" -> "公告管理";
            case "banners" -> "轮播图管理";
            case "delivery" -> "配送管理";
            case "reviews" -> "评价管理";
            case "inventory" -> "库存管理";
            case "statistics" -> "统计分析";
            case "settings" -> "店铺设置";
            default -> "系统管理";
        };
    }

    private String resolveAction(HttpServletRequest request, Method method) {
        String baseAction = Optional.ofNullable(method.getAnnotation(Operation.class))
            .map(Operation::summary)
            .filter(StringUtils::isNotBlank)
            .orElse(request.getMethod() + " " + request.getRequestURI());

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null || pathVariables.isEmpty()) {
            return baseAction;
        }

        String detail = pathVariables.entrySet()
            .stream()
            .map(entry -> formatIdentifier(entry.getKey(), entry.getValue()))
            .filter(StringUtils::isNotBlank)
            .reduce((left, right) -> left + "，" + right)
            .orElse("");

        return StringUtils.isBlank(detail) ? baseAction : baseAction + " [" + detail + "]";
    }

    private String formatIdentifier(String key, String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return switch (key) {
            case "id" -> "ID=" + value;
            case "orderNo" -> "订单号=" + value;
            case "deliveryId" -> "配送ID=" + value;
            default -> key + "=" + value;
        };
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
