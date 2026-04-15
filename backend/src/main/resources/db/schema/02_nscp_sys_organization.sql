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
