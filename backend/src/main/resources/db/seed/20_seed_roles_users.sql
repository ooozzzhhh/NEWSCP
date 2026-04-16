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
    (1, 21), (1, 22), (1, 23), (1, 24),
    (1, 37), (1, 38), (1, 39), (1, 40), (1, 41), (1, 42), (1, 43), (1, 44)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO nscp_sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
