-- =============================================================================
-- 阶段1（合并入口）：身份/组织表结构 + 菜单种子 + 管理员种子
-- 维护：只改 schema/ 与 seed/，再运行 merge_stage_sql.ps1 重新生成本文件。
-- 等价于：01_nscp_sys_identity + 02_nscp_sys_organization + 10_seed_permissions_menu + 20_seed_roles_users
-- =============================================================================
-- =============================================================================
-- 表结构：身份与授权（与实体 SysUser / SysRole / SysPermission 及关联表一致）
-- 依赖：无。执行前：USE nscp_dev;
-- 对照：backend/.../sys/user/entity/SysUser.java、sys/role/entity/*.java
-- =============================================================================

CREATE TABLE IF NOT EXISTS nscp_sys_user (
    id                  BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id           VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    username            VARCHAR(64)     NOT NULL                           COMMENT '登录用户名（租户内唯一）',
    password_hash       VARCHAR(256)    NOT NULL                           COMMENT '密码哈希（BCrypt）',
    real_name           VARCHAR(64)     NOT NULL                           COMMENT '真实姓名',
    email               VARCHAR(128)                                        COMMENT '邮箱',
    phone               VARCHAR(32)                                         COMMENT '手机号',
    avatar_url          VARCHAR(512)                                        COMMENT '头像URL',
    user_type           VARCHAR(32)     NOT NULL DEFAULT 'NORMAL'          COMMENT '用户类型',
    status              VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE'          COMMENT '状态',
    locked_until        DATETIME                                            COMMENT '锁定解锁时间',
    pwd_changed_at      DATETIME                                            COMMENT '密码最后修改时间',
    login_fail_count    INT             NOT NULL DEFAULT 0                 COMMENT '连续登录失败次数',
    last_login_at       DATETIME                                            COMMENT '最后登录成功时间',
    created_by          VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          VARCHAR(64)                                         COMMENT '最后修改人',
    updated_at          DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    deleted             TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_tenant_deleted (tenant_id, deleted)
) COMMENT '系统用户表';

CREATE TABLE IF NOT EXISTS nscp_sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id   VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    role_code   VARCHAR(64)     NOT NULL                           COMMENT '角色编码',
    role_name   VARCHAR(128)    NOT NULL                           COMMENT '角色名称',
    remark      VARCHAR(256)                                        COMMENT '备注',
    sort_order  INT             NOT NULL DEFAULT 0                 COMMENT '排序',
    created_by  VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by  VARCHAR(64)                                         COMMENT '修改人',
    updated_at  DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted     TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code),
    INDEX idx_tenant (tenant_id)
) COMMENT '系统角色表';

CREATE TABLE IF NOT EXISTS nscp_sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    parent_id       BIGINT          NOT NULL DEFAULT 0                 COMMENT '父节点ID',
    perm_type       VARCHAR(32)     NOT NULL                           COMMENT 'MENU/BUTTON',
    perm_code       VARCHAR(128)    NOT NULL                           COMMENT '权限编码',
    perm_name       VARCHAR(128)    NOT NULL                           COMMENT '权限名称',
    route_path      VARCHAR(256)                                         COMMENT '路由路径',
    component_path  VARCHAR(256)                                         COMMENT '组件路径',
    icon            VARCHAR(64)                                          COMMENT '图标',
    sort_order      INT             NOT NULL DEFAULT 0                 COMMENT '排序',
    is_hidden       TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '是否隐藏',
    status          VARCHAR(32)     NOT NULL DEFAULT 'ENABLED'         COMMENT '状态',
    created_by      VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(64)                                         COMMENT '修改人',
    updated_at      DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted         TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    UNIQUE KEY uk_perm_code (perm_code),
    INDEX idx_parent (parent_id),
    INDEX idx_perm_type (perm_type)
) COMMENT '权限/菜单资源表';

CREATE TABLE IF NOT EXISTS nscp_sys_role_permission (
    role_id     BIGINT  NOT NULL COMMENT '角色ID',
    perm_id     BIGINT  NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, perm_id),
    INDEX idx_perm_id (perm_id)
) COMMENT '角色-权限关联表';

CREATE TABLE IF NOT EXISTS nscp_sys_user_role (
    user_id     BIGINT  NOT NULL COMMENT '用户ID',
    role_id     BIGINT  NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户-角色关联表';


-- =============================================================================
-- 表结构：组织（与实体 SysDept 一致）
-- 依赖：schema/01_nscp_sys_identity.sql
-- 对照：backend/.../sys/dept/entity/SysDept.java
-- =============================================================================

CREATE TABLE IF NOT EXISTS nscp_sys_dept (
    id          BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    tenant_id   VARCHAR(64)     NOT NULL                           COMMENT '租户ID',
    dept_name   VARCHAR(128)    NOT NULL                           COMMENT '部门名称',
    parent_id   BIGINT          NOT NULL DEFAULT 0                 COMMENT '父部门ID',
    leader_id   BIGINT                                              COMMENT '部门负责人ID',
    sort_order  INT             NOT NULL DEFAULT 0                 COMMENT '排序',
    remark      VARCHAR(256)                                        COMMENT '备注',
    created_by  VARCHAR(64)     NOT NULL                           COMMENT '创建人',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by  VARCHAR(64)                                         COMMENT '修改人',
    updated_at  DATETIME                 ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleted     TINYINT(1)      NOT NULL DEFAULT 0                 COMMENT '逻辑删除',
    INDEX idx_tenant_parent (tenant_id, parent_id),
    INDEX idx_tenant_deleted (tenant_id, deleted)
) COMMENT '系统部门表';

CREATE TABLE IF NOT EXISTS nscp_sys_user_dept (
    user_id     BIGINT          NOT NULL COMMENT '用户ID',
    dept_id     BIGINT          NOT NULL COMMENT '部门ID',
    is_primary  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否主部门',
    PRIMARY KEY (user_id, dept_id),
    INDEX idx_dept_id (dept_id)
) COMMENT '用户-部门关联表';


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
    (26, 1, 'MENU', 'menu:sys:permission', '菜单权限运营', '/app/system/permissions', 'system/permissions/index', 'SafetyOutlined', 15, 0, 'ENABLED', 'seed', NOW(), 0)
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
    (36, 26, 'BUTTON', 'sys:perm:delete', '权限-删除', NULL, NULL, NULL, 4, 1, 'ENABLED', 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    perm_name = VALUES(perm_name),
    route_path = VALUES(route_path),
    component_path = VALUES(component_path),
    icon = VALUES(icon),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;


-- =============================================================================
-- 种子数据：内置管理员角色、admin 用户及基础授权（阶段1）
-- 依赖：seed/10_seed_permissions_menu.sql
-- 默认密码：Admin@2026（BCrypt，与 application.properties sys.default.password 一致）
-- =============================================================================

INSERT INTO nscp_sys_role (
    id, tenant_id, role_code, role_name, remark, sort_order, created_by, created_at, deleted
) VALUES
    (1, 'demo-tenant', 'ADMIN', '系统管理员', '种子数据', 0, 'seed', NOW(), 0)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    remark = VALUES(remark),
    sort_order = VALUES(sort_order),
    deleted = 0;

-- password_hash：BCrypt(Admin@2026)，与 BCryptPasswordEncoder 强度 12 一致
INSERT INTO nscp_sys_user (
    id, tenant_id, username, password_hash, real_name, email, phone,
    user_type, status, pwd_changed_at, login_fail_count, created_by, created_at, deleted
) VALUES (
    1, 'demo-tenant', 'admin',
    '$2b$12$fDmoOSn2sbvgwIOSd5rCzO2Fvqvib9lK1BVU5hRlrCiujWwnYx536',
    '系统管理员', NULL, NULL,
    'NORMAL', 'ACTIVE', NOW(), 0, 'seed', NOW(), 0
)
ON DUPLICATE KEY UPDATE
    real_name = VALUES(real_name),
    status = VALUES(status),
    deleted = 0;

INSERT INTO nscp_sys_role_permission (role_id, perm_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
    (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
    (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
    (1, 21), (1, 22), (1, 23), (1, 24)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

