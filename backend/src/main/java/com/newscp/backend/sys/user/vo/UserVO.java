package com.newscp.backend.sys.user.vo;

import java.time.LocalDateTime;
import java.util.List;

public record UserVO(
        Long id,
        String username,
        String realName,
        String email,
        String phone,
        String userType,
        String status,
        Long deptId,
        String deptName,
        List<RoleBriefVO> roles,
        LocalDateTime createdAt
) {
}
