package com.newscp.backend.sys.role.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRolePermissionMapper {

    @Delete("DELETE FROM nscp_sys_role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("""
            INSERT INTO nscp_sys_role_permission(role_id, perm_id)
            VALUES(#{roleId}, #{permId})
            """)
    void insert(@Param("roleId") Long roleId, @Param("permId") Long permId);

    @Select("SELECT perm_id FROM nscp_sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermIdsByRoleId(@Param("roleId") Long roleId);
}
