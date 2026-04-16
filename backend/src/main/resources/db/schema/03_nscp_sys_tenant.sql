-- =============================================================================
-- 表结构：租户（与实体 SysTenant、SysUserTenantMapper SQL 一致）
-- 依赖：schema/01_nscp_sys_identity.sql（user 主键被 nscp_sys_user_tenant 引用）
-- 对照：backend/.../tenant/entity/SysTenant.java、mapper/SysUserTenantMapper.java
-- =============================================================================

CREATE TABLE IF NOT EXISTS nscp_sys_tenant (
    id                  BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id           VARCHAR(64)     NOT NULL                           COMMENT '租户标识（UUID/编码）',
    tenant_name         VARCHAR(128)    NOT NULL                           COMMENT '租户名称',
    status              VARCHAR(32)     NOT NULL DEFAULT 'ENABLED'         COMMENT 'ENABLED/DISABLED',
    expire_at           DATETIME                                            COMMENT '到期时间（为空表示长期）',
    contact_name        VARCHAR(64)                                         COMMENT '联系人',
    contact_phone       VARCHAR(32)                                         COMMENT '联系人手机号',
    contact_email       VARCHAR(128)                                        COMMENT '联系人邮箱',
    remark              VARCHAR(512)                                        COMMENT '备注',
    created_by          VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          VARCHAR(64)                                         COMMENT '修改人',
    updated_at          DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted             TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_id (tenant_id),
    INDEX idx_status_deleted (status, deleted),
    INDEX idx_tenant_name (tenant_name)
) COMMENT '系统租户表';

CREATE TABLE IF NOT EXISTS nscp_sys_user_tenant (
    user_id             BIGINT          NOT NULL                           COMMENT '用户ID',
    tenant_id           VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    is_default          TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '是否默认租户',
    created_by          VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id, tenant_id),
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_user_default (user_id, is_default)
) COMMENT '用户-租户关联表';

CREATE TABLE IF NOT EXISTS nscp_sys_password_policy (
    id                          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id                   VARCHAR(64)     NOT NULL                           COMMENT '租户ID（每租户一条）',
    min_length                  INT             NOT NULL DEFAULT 8                 COMMENT '最小长度',
    max_length                  INT             NOT NULL DEFAULT 32                COMMENT '最大长度',
    require_digit               TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '要求包含数字',
    require_lower               TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '要求包含小写字母',
    require_upper               TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '要求包含大写字母',
    require_special             TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '要求包含特殊符号',
    expire_enabled              TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '是否启用密码有效期',
    expire_days                 INT             NOT NULL DEFAULT 90                COMMENT '密码有效天数',
    alert_before_days           INT             NOT NULL DEFAULT 7                 COMMENT '过期前预警天数',
    force_change_default        TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '首次使用默认密码时强制修改',
    force_change_on_rule_update TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '策略更新后强制不符合用户修改密码',
    lock_enabled                TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '是否启用登录失败锁定',
    lock_threshold              INT             NOT NULL DEFAULT 5                 COMMENT '连续失败N次后锁定',
    lock_duration               INT             NOT NULL DEFAULT 30                COMMENT '锁定时长（分钟）',
    auto_unlock                 TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '是否到期自动解锁',
    default_password            VARCHAR(128)    NOT NULL DEFAULT 'Admin@2026'      COMMENT '管理员重置密码时的默认密码（明文存储）',
    created_by                  VARCHAR(64)                                         COMMENT '创建人',
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by                  VARCHAR(64)                                         COMMENT '修改人',
    updated_at                  DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_tenant (tenant_id)
) COMMENT '密码与安全策略（每租户一条）';

CREATE TABLE IF NOT EXISTS nscp_sys_dict_type (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id   VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    type_code   VARCHAR(64)     NOT NULL                           COMMENT '类型编码',
    type_name   VARCHAR(128)    NOT NULL                           COMMENT '类型名称',
    source      VARCHAR(16)     NOT NULL DEFAULT 'CUSTOM'          COMMENT '来源：BUILTIN/CUSTOM',
    editable    TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '是否允许编辑',
    status      TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '1=启用, 0=禁用',
    sort_order  INT             NOT NULL DEFAULT 0                 COMMENT '排序',
    remark      VARCHAR(500)                                        COMMENT '备注',
    created_by  VARCHAR(64)                                         COMMENT '创建人',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by  VARCHAR(64)                                         COMMENT '修改人',
    updated_at  DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted     TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, type_code, deleted)
) COMMENT '枚举/字典类型';

CREATE TABLE IF NOT EXISTS nscp_sys_dict_item (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id   VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    type_code   VARCHAR(64)     NOT NULL                           COMMENT '所属类型编码',
    value       VARCHAR(64)     NOT NULL                           COMMENT '枚举值',
    label       VARCHAR(128)    NOT NULL                           COMMENT '显示文本',
    label_en    VARCHAR(128)                                        COMMENT '英文显示文本',
    color       VARCHAR(32)                                         COMMENT '前端颜色/样式',
    extra       VARCHAR(512)                                        COMMENT '扩展JSON',
    sort_order  INT             NOT NULL DEFAULT 0                 COMMENT '排序',
    status      TINYINT(1)      NOT NULL DEFAULT 1                 COMMENT '1=启用, 0=禁用',
    is_default  TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '是否默认项',
    remark      VARCHAR(500)                                        COMMENT '备注',
    created_by  VARCHAR(64)                                         COMMENT '创建人',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by  VARCHAR(64)                                         COMMENT '修改人',
    updated_at  DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted     TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_type_value (tenant_id, type_code, value, deleted),
    INDEX idx_type_code (tenant_id, type_code, status, sort_order)
) COMMENT '枚举/字典项';
