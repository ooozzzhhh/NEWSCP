package com.newscp.backend.sys.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PasswordPolicyDTO(
        @NotNull(message = "最小长度不能为空")
        @Min(value = 6, message = "最小长度不能小于6")
        @Max(value = 64, message = "最小长度不能大于64")
        Integer minLength,

        @NotNull(message = "最大长度不能为空")
        @Min(value = 6, message = "最大长度不能小于6")
        @Max(value = 64, message = "最大长度不能大于64")
        Integer maxLength,

        @NotNull(message = "requireDigit 不能为空")
        Integer requireDigit,

        @NotNull(message = "requireLower 不能为空")
        Integer requireLower,

        @NotNull(message = "requireUpper 不能为空")
        Integer requireUpper,

        @NotNull(message = "requireSpecial 不能为空")
        Integer requireSpecial,

        @NotNull(message = "expireEnabled 不能为空")
        Integer expireEnabled,

        @NotNull(message = "expireDays 不能为空")
        @Min(value = 1, message = "有效期天数必须大于0")
        Integer expireDays,

        @NotNull(message = "alertBeforeDays 不能为空")
        @Min(value = 0, message = "预警天数不能小于0")
        Integer alertBeforeDays,

        @NotNull(message = "forceChangeDefault 不能为空")
        Integer forceChangeDefault,

        @NotNull(message = "forceChangeOnRuleUpdate 不能为空")
        Integer forceChangeOnRuleUpdate,

        @NotNull(message = "lockEnabled 不能为空")
        Integer lockEnabled,

        @NotNull(message = "lockThreshold 不能为空")
        @Min(value = 1, message = "锁定阈值必须大于0")
        Integer lockThreshold,

        @NotNull(message = "lockDuration 不能为空")
        @Min(value = 1, message = "锁定时长必须大于0")
        Integer lockDuration,

        @NotNull(message = "autoUnlock 不能为空")
        Integer autoUnlock,

        @NotNull(message = "defaultPassword 不能为空")
        String defaultPassword
) {
}
