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
