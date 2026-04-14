-- NEWSCP 阶段1（用户/角色/权限/组织）数据库结构
-- 执行前请确认数据库已切换到目标 schema（例如 nscp_dev）

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
