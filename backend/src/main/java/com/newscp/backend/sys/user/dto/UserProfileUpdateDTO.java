package com.newscp.backend.sys.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateDTO(
        @NotBlank(message = "真实姓名不能为空") String realName,
        String email,
        String phone,
        String avatarUrl
) {
}
