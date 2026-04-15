-- =============================================================================
-- NEWSCP 阶段2：平台与安全底座补强（P0 首批）
-- 覆盖：2.1 租户与开通、2.2 菜单与权限运营
-- 执行方式：USE nscp_dev; source stage2_init.sql;
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 2.1 租户基础
-- ---------------------------------------------------------------------------

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

-- ---------------------------------------------------------------------------
-- 2.2 菜单与权限运营：补充菜单运营权限与入口
-- ---------------------------------------------------------------------------

INSERT INTO nscp_sys_permission (
    id, parent_id, perm_type, perm_code, perm_name, route_path, component_path, icon, sort_order, is_hidden, status, created_by, deleted
) VALUES
    (25, 1, 'MENU', 'menu:sys:tenant', '租户管理', '/app/system/tenants', 'system/tenants/index', 'BankOutlined', 14, 0, 'ENABLED', 'stage2', 0),
    (26, 1, 'MENU', 'menu:sys:permission', '菜单权限运营', '/app/system/permissions', 'system/permissions/index', 'SafetyOutlined', 15, 0, 'ENABLED', 'stage2', 0)
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    route_path = VALUES(route_path),
    component_path = VALUES(component_path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;

INSERT INTO nscp_sys_permission (
    id, parent_id, perm_type, perm_code, perm_name, route_path, component_path, icon, sort_order, is_hidden, status, created_by, deleted
) VALUES
    (27, 25, 'BUTTON', 'sys:tenant:list', '租户-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'stage2', 0),
    (28, 25, 'BUTTON', 'sys:tenant:create', '租户-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'stage2', 0),
    (29, 25, 'BUTTON', 'sys:tenant:read', '租户-查看', NULL, NULL, NULL, 3, 1, 'ENABLED', 'stage2', 0),
    (30, 25, 'BUTTON', 'sys:tenant:update', '租户-编辑', NULL, NULL, NULL, 4, 1, 'ENABLED', 'stage2', 0),
    (31, 25, 'BUTTON', 'sys:tenant:delete', '租户-删除', NULL, NULL, NULL, 5, 1, 'ENABLED', 'stage2', 0),
    (32, 25, 'BUTTON', 'sys:tenant:assign-user', '租户-分配用户', NULL, NULL, NULL, 6, 1, 'ENABLED', 'stage2', 0),
    (33, 26, 'BUTTON', 'sys:perm:list', '权限-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'stage2', 0),
    (34, 26, 'BUTTON', 'sys:perm:create', '权限-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'stage2', 0),
    (35, 26, 'BUTTON', 'sys:perm:update', '权限-编辑', NULL, NULL, NULL, 3, 1, 'ENABLED', 'stage2', 0),
    (36, 26, 'BUTTON', 'sys:perm:delete', '权限-删除', NULL, NULL, NULL, 4, 1, 'ENABLED', 'stage2', 0)
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;

-- 默认把阶段2 P0 权限赋给 ADMIN 角色（id=1）
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

-- 租户种子：超级管理员租户 admin（按 tenant_id 幂等）
INSERT INTO nscp_sys_tenant (
    tenant_id, tenant_name, status, expire_at, contact_name, contact_phone, contact_email, remark, created_by, created_at, deleted
) VALUES (
    'admin', '超级管理员租户', 'ENABLED', NULL, '系统管理员', NULL, NULL, '平台超级管理员租户', 'stage2', NOW(), 0
)
ON DUPLICATE KEY UPDATE
    tenant_name = VALUES(tenant_name),
    status = VALUES(status),
    deleted = 0;

INSERT INTO nscp_sys_user_tenant (user_id, tenant_id, is_default, created_by, created_at) VALUES
    (1, 'admin', 1, 'stage2', NOW())
ON DUPLICATE KEY UPDATE
    is_default = VALUES(is_default);

UPDATE nscp_sys_user
SET tenant_id = 'admin'
WHERE id = 1;

-- 超级管理员角色（全租户管理）与租户管理员角色（租户内管理）
INSERT INTO nscp_sys_role (
    id, tenant_id, role_code, role_name, remark, sort_order, created_by, deleted
) VALUES
    (2, 'admin', 'SUPER_ADMIN_ROLE', '超级管理员', '可管理所有租户与功能', 0, 'stage2', 0),
    (3, 'admin', 'TENANT_ADMIN_ROLE', '租户管理员', '可管理所属租户下用户和菜单', 1, 'stage2', 0)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    remark = VALUES(remark),
    sort_order = VALUES(sort_order),
    deleted = 0;

-- 超级管理员拥有全部已配置权限
INSERT INTO nscp_sys_role_permission (role_id, perm_id)
SELECT 2, id
FROM nscp_sys_permission
WHERE deleted = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 租户管理员最小权限（用户、角色、部门、菜单权限运营）
INSERT INTO nscp_sys_role_permission (role_id, perm_id) VALUES
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 26),
    (3, 9), (3, 10), (3, 11), (3, 12), (3, 13), (3, 14),
    (3, 15), (3, 16), (3, 17), (3, 18), (3, 19), (3, 20),
    (3, 21), (3, 22), (3, 23), (3, 24),
    (3, 33), (3, 34), (3, 35), (3, 36)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_user_role (user_id, role_id) VALUES (1, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
