package com.newscp.backend.auth.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String userId,
        String username,
        String tenantId,
        List<String> roles
) {
}
