package com.newscp.backend.tenant.dto;

import java.util.List;

public record TenantUserAssignDTO(
        List<Long> userIds,
        Long defaultUserId
) {
}
