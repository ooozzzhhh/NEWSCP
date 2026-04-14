package com.newscp.backend.sys.role.vo;

import java.util.List;

public record RoleDetailVO(
        Long id,
        String roleCode,
        String roleName,
        String remark,
        Integer sortOrder,
        List<Long> permIds
) {
}
