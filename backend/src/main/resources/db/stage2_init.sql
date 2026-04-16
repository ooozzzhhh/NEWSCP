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

