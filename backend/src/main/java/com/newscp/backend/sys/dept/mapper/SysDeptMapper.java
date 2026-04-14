package com.newscp.backend.sys.dept.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.dept.entity.SysDept;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("""
            SELECT d.id AS id,
                   d.dept_name AS deptName,
                   d.parent_id AS parentId,
                   d.leader_id AS leaderId,
                   u.real_name AS leaderName,
                   d.sort_order AS sortOrder,
                   d.remark AS remark
            FROM nscp_sys_dept d
            LEFT JOIN nscp_sys_user u ON u.id = d.leader_id AND u.deleted = 0
            WHERE d.deleted = 0
            ORDER BY d.parent_id ASC, d.sort_order ASC, d.id ASC
            """)
    List<DeptFlatRow> selectFlatRows();

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_dept
            WHERE parent_id = #{parentId}
              AND deleted = 0
            """)
    long countChildren(@Param("parentId") Long parentId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_dept
            WHERE parent_id = #{parentId}
              AND dept_name = #{deptName}
              AND deleted = 0
            """)
    long countSiblingName(@Param("parentId") Long parentId, @Param("deptName") String deptName);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_dept
            WHERE parent_id = #{parentId}
              AND dept_name = #{deptName}
              AND id <> #{excludeId}
              AND deleted = 0
            """)
    long countSiblingNameExclude(@Param("parentId") Long parentId, @Param("deptName") String deptName, @Param("excludeId") Long excludeId);

    record DeptFlatRow(
            Long id,
            String deptName,
            Long parentId,
            Long leaderId,
            String leaderName,
            Integer sortOrder,
            String remark
    ) {
    }
}
