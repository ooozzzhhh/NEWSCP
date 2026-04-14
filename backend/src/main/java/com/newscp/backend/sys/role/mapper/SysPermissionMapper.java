package com.newscp.backend.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.role.entity.SysPermission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("""
            SELECT DISTINCT p.*
            FROM nscp_sys_user_role ur
            JOIN nscp_sys_role_permission rp ON rp.role_id = ur.role_id
            JOIN nscp_sys_permission p ON p.id = rp.perm_id
            WHERE ur.user_id = #{userId}
              AND p.deleted = 0
              AND p.status = 'ENABLED'
            ORDER BY p.parent_id ASC, p.sort_order ASC, p.id ASC
            """)
    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.perm_code
            FROM nscp_sys_user_role ur
            JOIN nscp_sys_role_permission rp ON rp.role_id = ur.role_id
            JOIN nscp_sys_permission p ON p.id = rp.perm_id
            WHERE ur.user_id = #{userId}
              AND p.deleted = 0
              AND p.status = 'ENABLED'
            ORDER BY p.perm_code ASC
            """)
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT *
            FROM nscp_sys_permission
            WHERE deleted = 0
              AND status = 'ENABLED'
            ORDER BY parent_id ASC, sort_order ASC, id ASC
            """)
    List<SysPermission> selectAllEnabled();
}
