package com.newscp.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ForceChangePasswordRequest(
        @NotBlank(message = "username is required")
        String username,
        @NotBlank(message = "oldPassword is required")
        String oldPassword,
        @NotBlank(message = "newPassword is required")
        String newPassword,
        @NotBlank(message = "confirmPassword is required")
        String confirmPassword,
        String tenantId
) {
}
