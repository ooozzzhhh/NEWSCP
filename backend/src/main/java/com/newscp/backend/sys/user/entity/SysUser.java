package com.newscp.backend.sys.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("nscp_sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private String tenantId;

    private String username;
    private String passwordHash;
    private String realName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String userType;
    private String status;
    private LocalDateTime lockedUntil;
    private LocalDateTime pwdChangedAt;
    private Integer loginFailCount;
    private LocalDateTime lastLoginAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
