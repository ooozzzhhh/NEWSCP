package com.newscp.backend.sys.role.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserRoleMapper {

    @Delete("DELETE FROM nscp_sys_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM nscp_sys_user_role WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("""
            INSERT INTO nscp_sys_user_role(user_id, role_id)
            VALUES(#{userId}, #{roleId})
            """)
    void insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("SELECT role_id FROM nscp_sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select({
            "<script>",
            "SELECT ur.user_id AS userId, r.id AS roleId, r.role_name AS roleName",
            "FROM nscp_sys_user_role ur",
            "JOIN nscp_sys_role r ON r.id = ur.role_id",
            "WHERE r.deleted = 0",
            "AND ur.user_id IN",
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "ORDER BY r.sort_order ASC, r.id ASC",
            "</script>"
    })
    List<UserRoleBindRow> selectRoleRowsByUserIds(@Param("userIds") List<Long> userIds);

    record UserRoleBindRow(Long userId, Long roleId, String roleName) {
    }
}
