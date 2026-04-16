-- =============================================================================
-- 种子数据：租户表数据、平台级角色、用户-租户绑定（阶段2 / AuthService 默认租户 admin）
-- 依赖：schema/03_nscp_sys_tenant.sql、seed/20_seed_roles_users.sql、seed/10_seed_permissions_menu.sql
-- =============================================================================

-- ADMIN（id=1）补充租户/权限运营相关按钮
INSERT INTO nscp_sys_role_permission (role_id, perm_id) VALUES
    (1, 25), (1, 26),
    (1, 27), (1, 28), (1, 29), (1, 30), (1, 31), (1, 32),
    (1, 33), (1, 34), (1, 35), (1, 36),
    (1, 37), (1, 38), (1, 39), (1, 40), (1, 41), (1, 42), (1, 43), (1, 44)
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
    (3, 33), (3, 34), (3, 35), (3, 36),
    (3, 37), (3, 38), (3, 39), (3, 40), (3, 41), (3, 42), (3, 43), (3, 44)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_password_policy (
    tenant_id, min_length, max_length, require_digit, require_lower, require_upper, require_special,
    expire_enabled, expire_days, alert_before_days,
    force_change_default, force_change_on_rule_update,
    lock_enabled, lock_threshold, lock_duration, auto_unlock,
    default_password, created_by, created_at
) VALUES (
    'admin', 8, 32, 1, 1, 0, 0,
    0, 90, 7,
    1, 0,
    1, 5, 30, 1,
    'Admin@2026', 'seed', NOW()
)
ON DUPLICATE KEY UPDATE
    min_length = VALUES(min_length),
    max_length = VALUES(max_length),
    require_digit = VALUES(require_digit),
    require_lower = VALUES(require_lower),
    require_upper = VALUES(require_upper),
    require_special = VALUES(require_special),
    expire_enabled = VALUES(expire_enabled),
    expire_days = VALUES(expire_days),
    alert_before_days = VALUES(alert_before_days),
    force_change_default = VALUES(force_change_default),
    force_change_on_rule_update = VALUES(force_change_on_rule_update),
    lock_enabled = VALUES(lock_enabled),
    lock_threshold = VALUES(lock_threshold),
    lock_duration = VALUES(lock_duration),
    auto_unlock = VALUES(auto_unlock),
    default_password = VALUES(default_password);

INSERT INTO nscp_sys_dict_type (tenant_id, type_code, type_name, source, editable, status, sort_order, created_by, created_at, deleted) VALUES
    ('admin', 'USER_STATUS', '用户状态', 'BUILTIN', 0, 1, 1, 'seed', NOW(), 0),
    ('admin', 'TENANT_STATUS', '租户状态', 'BUILTIN', 0, 1, 2, 'seed', NOW(), 0),
    ('admin', 'ORDER_STATUS', '订单状态', 'BUILTIN', 1, 1, 10, 'seed', NOW(), 0),
    ('admin', 'FORECAST_STATUS', '预测状态', 'BUILTIN', 1, 1, 11, 'seed', NOW(), 0),
    ('admin', 'PRODUCT_TYPE', '产品类型', 'CUSTOM', 1, 1, 20, 'seed', NOW(), 0),
    ('admin', 'STOCK_POINT_TYPE', '库存点类型', 'CUSTOM', 1, 1, 21, 'seed', NOW(), 0),
    ('admin', 'APPROVAL_STATUS', '审批状态', 'BUILTIN', 0, 1, 30, 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    type_name = VALUES(type_name),
    source = VALUES(source),
    editable = VALUES(editable),
    status = VALUES(status),
    sort_order = VALUES(sort_order),
    deleted = 0;

INSERT INTO nscp_sys_dict_item (tenant_id, type_code, value, label, color, sort_order, status, is_default, created_by, created_at, deleted) VALUES
    ('admin', 'USER_STATUS', 'ACTIVE', '正常', 'success', 1, 1, 0, 'seed', NOW(), 0),
    ('admin', 'USER_STATUS', 'DISABLED', '禁用', 'destructive', 2, 1, 0, 'seed', NOW(), 0),
    ('admin', 'USER_STATUS', 'LOCKED', '锁定', 'warning', 3, 1, 0, 'seed', NOW(), 0),
    ('admin', 'TENANT_STATUS', 'ENABLED', '启用', 'success', 1, 1, 0, 'seed', NOW(), 0),
    ('admin', 'TENANT_STATUS', 'DISABLED', '停用', 'destructive', 2, 1, 0, 'seed', NOW(), 0),
    ('admin', 'APPROVAL_STATUS', 'PENDING', '待审批', 'warning', 1, 1, 1, 'seed', NOW(), 0),
    ('admin', 'APPROVAL_STATUS', 'APPROVED', '已通过', 'success', 2, 1, 0, 'seed', NOW(), 0),
    ('admin', 'APPROVAL_STATUS', 'REJECTED', '已拒绝', 'destructive', 3, 1, 0, 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    label = VALUES(label),
    color = VALUES(color),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    is_default = VALUES(is_default),
    deleted = 0;

INSERT INTO nscp_sys_user_role (user_id, role_id) VALUES (1, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
