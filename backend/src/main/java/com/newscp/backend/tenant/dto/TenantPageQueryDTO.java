package com.newscp.backend.tenant.dto;

public record TenantPageQueryDTO(
        Integer page,
        Integer size,
        String keyword,
        String status
) {
    public int safePage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
