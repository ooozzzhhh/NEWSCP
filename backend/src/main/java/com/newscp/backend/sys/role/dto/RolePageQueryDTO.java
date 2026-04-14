package com.newscp.backend.sys.role.dto;

public record RolePageQueryDTO(
        Integer page,
        Integer size,
        String roleName
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
