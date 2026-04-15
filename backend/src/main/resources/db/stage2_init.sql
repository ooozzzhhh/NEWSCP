-- =============================================================================
-- 阶段2（合并入口）：租户表结构 + 平台租户/角色种子
-- 等价于：03_nscp_sys_tenant + 30_seed_tenant_platform
-- =============================================================================
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


-- =============================================================================
-- 种子数据：租户表数据、平台级角色、用户-租户绑定（阶段2 / AuthService 默认租户 admin）
-- 依赖：schema/03_nscp_sys_tenant.sql、seed/20_seed_roles_users.sql、seed/10_seed_permissions_menu.sql
-- =============================================================================

-- ADMIN（id=1）补充租户/权限运营相关按钮
INSERT INTO nscp_sys_role_permission (role_id, perm_id) VALUES
    (1, 25), (1, 26),
    (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32),
    (1, 33), (1, 34), (1, 35), (1, 36)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 历史兼容：若早期脚本把超管租户写成 demo-tenant 且 admin 不存在，则修正为 admin
UPDATE nscp_sys_tenant t
SET t.tenant_id = 'admin'
WHERE t.tenant_id = 'demo-tenant'
  AND t.tenant_name = '超级管理员租户'
  AND NOT EXISTS (
      SELECT 1
      FROM (SELECT tenant_id FROM nscp_sys_tenant WHERE tenant_id = 'admin' AND deleted = 0) x
  );

INSERT INTO nscp_sys_tenant (
    tenant_id, tenant_name, status, expire_at, contact_name, contact_phone, contact_email, remark, created_by, created_at, deleted
) VALUES (
    'admin', '超级管理员租户', 'ENABLED', NULL, '系统管理员', NULL, NULL, '平台超级管理员租户', 'seed', NOW(), 0
)
ON DUPLICATE KEY UPDATE
    tenant_name = VALUES(tenant_name),
    status = VALUES(status),
    deleted = 0;

INSERT INTO nscp_sys_user_tenant (user_id, tenant_id, is_default, created_by, created_at) VALUES
    (1, 'admin', 1, 'seed', NOW())
ON DUPLICATE KEY UPDATE
    is_default = VALUES(is_default);

UPDATE nscp_sys_user
SET tenant_id = 'admin'
WHERE id = 1;

INSERT INTO nscp_sys_role (
    id, tenant_id, role_code, role_name, remark, sort_order, created_by, created_at, deleted
) VALUES
    (2, 'admin', 'SUPER_ADMIN_ROLE', '超级管理员', '可管理所有租户与功能', 0, 'seed', NOW(), 0),
    (3, 'admin', 'TENANT_ADMIN_ROLE', '租户管理员', '可管理所属租户下用户和菜单', 1, 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    remark = VALUES(remark),
    sort_order = VALUES(sort_order),
    deleted = 0;

INSERT INTO nscp_sys_role_permission (role_id, perm_id)
SELECT 2, id
FROM nscp_sys_permission
WHERE deleted = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_role_permission (role_id, perm_id) VALUES
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 26),
    (3, 9), (3, 10), (3, 11), (3, 12), (3, 13), (3, 14),
    (3, 15), (3, 16), (3, 17), (3, 18), (3, 19), (3, 20),
    (3, 21), (3, 22), (3, 23), (3, 24),
    (3, 33), (3, 34), (3, 35), (3, 36)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_user_role (user_id, role_id) VALUES (1, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

