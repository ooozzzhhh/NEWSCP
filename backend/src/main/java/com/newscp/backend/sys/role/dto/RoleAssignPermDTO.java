package com.newscp.backend.sys.role.dto;

import java.util.List;

public record RoleAssignPermDTO(
        List<Long> permIds
) {
}
