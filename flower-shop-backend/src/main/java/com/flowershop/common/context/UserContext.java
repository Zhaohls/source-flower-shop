package com.flowershop.common.context;

public record UserContext(Long userId, String username, String role, String token) {
}
