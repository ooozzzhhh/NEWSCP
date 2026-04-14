package com.newscp.backend.sys.dept.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserDeptMapper {

    @Delete("DELETE FROM nscp_sys_user_dept WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO nscp_sys_user_dept(user_id, dept_id, is_primary)
            VALUES(#{userId}, #{deptId}, #{isPrimary})
            """)
    void insert(@Param("userId") Long userId, @Param("deptId") Long deptId, @Param("isPrimary") Integer isPrimary);

    @Select("SELECT dept_id FROM nscp_sys_user_dept WHERE user_id = #{userId} AND is_primary = 1 LIMIT 1")
    Long selectPrimaryDeptIdByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_user_dept
            WHERE dept_id = #{deptId}
              AND is_primary = 1
            """)
    long countUsersByDeptId(@Param("deptId") Long deptId);

    @Select("""
            SELECT u.id AS userId,
                   u.username AS username,
                   u.real_name AS realName,
                   u.status AS status
            FROM nscp_sys_user_dept ud
            JOIN nscp_sys_user u ON u.id = ud.user_id
            WHERE ud.dept_id = #{deptId}
              AND ud.is_primary = 1
              AND u.deleted = 0
            ORDER BY u.id ASC
            """)
    List<DeptUserRow> selectDeptUsers(@Param("deptId") Long deptId);

    @Select({
            "<script>",
            "SELECT ud.user_id AS userId, d.id AS deptId, d.dept_name AS deptName",
            "FROM nscp_sys_user_dept ud",
            "JOIN nscp_sys_dept d ON d.id = ud.dept_id",
            "WHERE d.deleted = 0",
            "AND ud.is_primary = 1",
            "AND ud.user_id IN",
            "<foreach item='id' collection='userIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<UserDeptRow> selectUserDeptRows(@Param("userIds") List<Long> userIds);

    @Select("""
            SELECT dept_id
            FROM nscp_sys_user_dept
            WHERE user_id = #{userId}
            """)
    List<Long> selectDeptIdsByUserId(@Param("userId") Long userId);

    record UserDeptRow(Long userId, Long deptId, String deptName) {
    }

    record DeptUserRow(Long userId, String username, String realName, String status) {
    }
}
