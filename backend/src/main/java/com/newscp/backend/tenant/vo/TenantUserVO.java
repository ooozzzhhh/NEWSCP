package com.newscp.backend.tenant.vo;

public record TenantUserVO(
        Long userId,
        String username,
        String realName,
        String userStatus,
        boolean isDefault
) {
}
