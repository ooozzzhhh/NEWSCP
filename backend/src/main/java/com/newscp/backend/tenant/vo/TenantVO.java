package com.newscp.backend.tenant.vo;

import java.time.LocalDateTime;

public record TenantVO(
        Long id,
        String tenantId,
        String tenantName,
        String status,
        LocalDateTime expireAt,
        String contactName,
        String contactPhone,
        String contactEmail,
        String remark,
        long userCount,
        LocalDateTime createdAt
) {
}
