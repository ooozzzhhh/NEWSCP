package com.newscp.backend.sys.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_password_policy")
public class PasswordPolicy {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tenantId;
    private Integer minLength;
    private Integer maxLength;
    private Integer requireDigit;
    private Integer requireLower;
    private Integer requireUpper;
    private Integer requireSpecial;
    private Integer expireEnabled;
    private Integer expireDays;
    private Integer alertBeforeDays;
    private Integer forceChangeDefault;
    private Integer forceChangeOnRuleUpdate;
    private Integer lockEnabled;
    private Integer lockThreshold;
    private Integer lockDuration;
    private Integer autoUnlock;
    private String defaultPassword;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
