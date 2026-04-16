package com.newscp.backend.sys.security.vo;

public record PasswordPolicyVO(
        Long id,
        String tenantId,
        Integer minLength,
        Integer maxLength,
        Integer requireDigit,
        Integer requireLower,
        Integer requireUpper,
        Integer requireSpecial,
        Integer expireEnabled,
        Integer expireDays,
        Integer alertBeforeDays,
        Integer forceChangeDefault,
        Integer forceChangeOnRuleUpdate,
        Integer lockEnabled,
        Integer lockThreshold,
        Integer lockDuration,
        Integer autoUnlock,
        String defaultPassword
) {
}
