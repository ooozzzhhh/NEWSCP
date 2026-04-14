package com.newscp.backend.sys.role.vo;

public record RoleVO(
        Long id,
        String roleCode,
        String roleName,
        String remark,
        Integer sortOrder,
        long userCount
) {
}
