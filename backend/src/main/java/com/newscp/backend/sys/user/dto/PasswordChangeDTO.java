package com.newscp.backend.sys.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeDTO(
        @NotBlank(message = "旧密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") String newPassword,
        @NotBlank(message = "确认密码不能为空") String confirmPassword
) {
}
