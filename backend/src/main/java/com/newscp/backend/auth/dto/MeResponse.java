package com.newscp.backend.auth.dto;

import java.util.List;

public record MeResponse(
        String userId,
        String username,
        String realName,
        String tenantId,
        List<String> roles,
        List<String> permissions
) {
}
