-- =============================================================================
-- 种子数据：菜单与按钮权限（perm_code 与前端路由、后端 @PreAuthorize 约定一致）
-- 依赖：schema/01_nscp_sys_identity.sql
-- 说明：显式写入 created_at，兼容 sql_mode 下无默认值的表结构
-- =============================================================================

INSERT INTO nscp_sys_permission (
    id, parent_id, perm_type, perm_code, perm_name, route_path, component_path, icon, sort_order, is_hidden, status, created_by, created_at, deleted
) VALUES
    (1, 0, 'MENU', 'menu:sys', '系统管理', NULL, NULL, 'SettingOutlined', 10, 0, 'ENABLED', 'seed', NOW(), 0),
    (2, 1, 'MENU', 'menu:sys:user', '用户管理', '/app/system/users', 'system/users/index', 'UserOutlined', 11, 0, 'ENABLED', 'seed', NOW(), 0),
    (3, 1, 'MENU', 'menu:sys:role', '角色管理', '/app/system/roles', 'system/roles/index', 'TeamOutlined', 12, 0, 'ENABLED', 'seed', NOW(), 0),
    (4, 1, 'MENU', 'menu:sys:dept', '部门管理', '/app/system/depts', 'system/depts/index', 'ApartmentOutlined', 13, 0, 'ENABLED', 'seed', NOW(), 0),
    (5, 0, 'MENU', 'menu:master', '主数据', NULL, NULL, 'DatabaseOutlined', 20, 0, 'ENABLED', 'seed', NOW(), 0),
    (6, 5, 'MENU', 'menu:master:product', '产品管理', '/app/master/products', 'master/products/index', NULL, 21, 0, 'ENABLED', 'seed', NOW(), 0),
    (7, 5, 'MENU', 'menu:master:stock', '库存点管理', '/app/master/stock-point', 'master/stock-point/index', NULL, 22, 0, 'ENABLED', 'seed', NOW(), 0),
    (8, 5, 'MENU', 'menu:master:customer', '客户管理', '/app/master/customer', 'master/customer/index', NULL, 23, 0, 'ENABLED', 'seed', NOW(), 0),
    (25, 1, 'MENU', 'menu:sys:tenant', '租户管理', '/app/system/tenants', 'system/tenants/index', 'BankOutlined', 14, 0, 'ENABLED', 'seed', NOW(), 0),
    (26, 1, 'MENU', 'menu:sys:permission', '菜单权限运营', '/app/system/permissions', 'system/permissions/index', 'SafetyOutlined', 15, 0, 'ENABLED', 'seed', NOW(), 0),
    (37, 1, 'MENU', 'menu:sys:security', '安全策略', '/app/system/security-policy', 'system/security-policy/index', 'SafetyCertificateOutlined', 16, 0, 'ENABLED', 'seed', NOW(), 0),
    (40, 1, 'MENU', 'menu:sys:dict', '枚举字典', '/app/system/dicts', 'system/dicts/index', 'TagsOutlined', 17, 0, 'ENABLED', 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    route_path = VALUES(route_path),
    component_path = VALUES(component_path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;

INSERT INTO nscp_sys_permission (
    id, parent_id, perm_type, perm_code, perm_name, route_path, component_path, icon, sort_order, is_hidden, status, created_by, created_at, deleted
) VALUES
    (9, 2, 'BUTTON', 'sys:user:list', '用户-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (10, 2, 'BUTTON', 'sys:user:create', '用户-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (11, 2, 'BUTTON', 'sys:user:read', '用户-查看', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (12, 2, 'BUTTON', 'sys:user:update', '用户-编辑', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0),
    (13, 2, 'BUTTON', 'sys:user:delete', '用户-删除', NULL, NULL, NULL, 5, 1, 'ENABLED', 'seed', NOW(), 0),
    (14, 2, 'BUTTON', 'sys:user:reset-pwd', '用户-重置密码', NULL, NULL, NULL, 6, 1, 'ENABLED', 'seed', NOW(), 0),
    (15, 3, 'BUTTON', 'sys:role:list', '角色-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (16, 3, 'BUTTON', 'sys:role:create', '角色-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (17, 3, 'BUTTON', 'sys:role:read', '角色-查看', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (18, 3, 'BUTTON', 'sys:role:update', '角色-编辑', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0),
    (19, 3, 'BUTTON', 'sys:role:delete', '角色-删除', NULL, NULL, NULL, 5, 1, 'ENABLED', 'seed', NOW(), 0),
    (20, 3, 'BUTTON', 'sys:role:assign-perm', '角色-分配权限', NULL, NULL, NULL, 6, 1, 'ENABLED', 'seed', NOW(), 0),
    (21, 4, 'BUTTON', 'sys:dept:list', '部门-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (22, 4, 'BUTTON', 'sys:dept:create', '部门-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (23, 4, 'BUTTON', 'sys:dept:update', '部门-编辑', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (24, 4, 'BUTTON', 'sys:dept:delete', '部门-删除', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0),
    (27, 25, 'BUTTON', 'sys:tenant:list', '租户-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (28, 25, 'BUTTON', 'sys:tenant:create', '租户-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (29, 25, 'BUTTON', 'sys:tenant:read', '租户-查看', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (30, 25, 'BUTTON', 'sys:tenant:update', '租户-编辑', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0),
    (31, 25, 'BUTTON', 'sys:tenant:delete', '租户-删除', NULL, NULL, NULL, 5, 1, 'ENABLED', 'seed', NOW(), 0),
    (32, 25, 'BUTTON', 'sys:tenant:assign-user', '租户-分配用户', NULL, NULL, NULL, 6, 1, 'ENABLED', 'seed', NOW(), 0),
    (33, 26, 'BUTTON', 'sys:perm:list', '权限-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (34, 26, 'BUTTON', 'sys:perm:create', '权限-新增', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (35, 26, 'BUTTON', 'sys:perm:update', '权限-编辑', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (36, 26, 'BUTTON', 'sys:perm:delete', '权限-删除', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0),
    (38, 37, 'BUTTON', 'sys:security:view', '安全策略-查看', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (39, 37, 'BUTTON', 'sys:security:edit', '安全策略-编辑', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (41, 40, 'BUTTON', 'sys:dict:list', '字典-列表', NULL, NULL, NULL, 1, 1, 'ENABLED', 'seed', NOW(), 0),
    (42, 40, 'BUTTON', 'sys:dict:create', '字典-新增类型', NULL, NULL, NULL, 2, 1, 'ENABLED', 'seed', NOW(), 0),
    (43, 40, 'BUTTON', 'sys:dict:edit', '字典-编辑', NULL, NULL, NULL, 3, 1, 'ENABLED', 'seed', NOW(), 0),
    (44, 40, 'BUTTON', 'sys:dict:delete', '字典-删除类型', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    route_path = VALUES(route_path),
    component_path = VALUES(component_path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;
