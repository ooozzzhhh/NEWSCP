package com.newscp.backend.sys.user.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserDetailVO(
        Long id,
        String tenantId,
        String username,
        String realName,
        String email,
        String phone,
        String avatarUrl,
        String userType,
        String status,
        LocalDateTime lockedUntil,
        LocalDateTime pwdChangedAt,
        Long deptId,
        String deptName,
        List<Long> roleIds,
        LocalDateTime createdAt
) {
}
