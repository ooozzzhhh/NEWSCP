package com.newscp.backend.sys.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.role.entity.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            SELECT r.role_code
            FROM nscp_sys_user_role ur
            JOIN nscp_sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.deleted = 0
              AND r.tenant_id = #{tenantId}
            ORDER BY r.sort_order ASC, r.id ASC
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId, @Param("tenantId") String tenantId);

    @Select("""
            SELECT r.role_code
            FROM nscp_sys_user_role ur
            JOIN nscp_sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.deleted = 0
            ORDER BY r.sort_order ASC, r.id ASC
            """)
    List<String> selectRoleCodesByUserIdIgnoreTenant(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_user_role
            WHERE role_id = #{roleId}
            """)
    long countUsersByRoleId(@Param("roleId") Long roleId);
}
