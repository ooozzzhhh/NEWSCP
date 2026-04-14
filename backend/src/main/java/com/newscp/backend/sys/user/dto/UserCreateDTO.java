package com.newscp.backend.sys.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UserCreateDTO(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "真实姓名不能为空") String realName,
        String email,
        String phone,
        String userType,
        Long deptId,
        List<Long> roleIds
) {
}
