package com.newscp.backend.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public record TenantCreateDTO(
        @NotBlank(message = "租户ID不能为空") String tenantId,
        @NotBlank(message = "租户名称不能为空") String tenantName,
        String status,
        LocalDateTime expireAt,
        String contactName,
        String contactPhone,
        String contactEmail,
        String remark,
        List<Long> userIds,
        Long defaultUserId
) {
}
