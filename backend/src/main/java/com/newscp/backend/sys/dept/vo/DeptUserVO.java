package com.newscp.backend.sys.dept.vo;

public record DeptUserVO(
        Long userId,
        String username,
        String realName,
        String status
) {
}
