package com.newscp.backend.tenant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserTenantMapper {

    @Delete("DELETE FROM nscp_sys_user_tenant WHERE tenant_id = #{tenantId}")
    void deleteByTenantId(@Param("tenantId") String tenantId);

    @Delete("DELETE FROM nscp_sys_user_tenant WHERE user_id = #{userId} AND tenant_id = #{tenantId}")
    void deleteByUserAndTenant(@Param("userId") Long userId, @Param("tenantId") String tenantId);

    @Insert("""
            INSERT INTO nscp_sys_user_tenant(user_id, tenant_id, is_default, created_by, created_at)
            VALUES(#{userId}, #{tenantId}, #{isDefault}, #{createdBy}, NOW())
            """)
    void insert(
            @Param("userId") Long userId,
            @Param("tenantId") String tenantId,
            @Param("isDefault") Integer isDefault,
            @Param("createdBy") String createdBy
    );

    @Select("""
            SELECT ut.user_id AS userId,
                   u.username AS username,
                   u.real_name AS realName,
                   u.status AS userStatus,
                   ut.is_default AS isDefault
            FROM nscp_sys_user_tenant ut
            JOIN nscp_sys_user u ON u.id = ut.user_id
            WHERE ut.tenant_id = #{tenantId}
              AND u.deleted = 0
            ORDER BY ut.is_default DESC, u.id ASC
            """)
    List<TenantUserBindRow> selectUsersByTenantId(@Param("tenantId") String tenantId);

    @Select("""
            SELECT tenant_id
            FROM nscp_sys_user_tenant
            WHERE user_id = #{userId}
              AND is_default = 1
            LIMIT 1
            """)
    String selectDefaultTenantByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_user_tenant
            WHERE tenant_id = #{tenantId}
              AND is_default = 1
            """)
    long countDefaultByTenant(@Param("tenantId") String tenantId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_user_tenant
            WHERE tenant_id = #{tenantId}
            """)
    long countUsersByTenantId(@Param("tenantId") String tenantId);

    @Select("""
            SELECT COUNT(*)
            FROM nscp_sys_user_tenant
            WHERE user_id = #{userId}
              AND tenant_id = #{tenantId}
            """)
    long countBind(@Param("userId") Long userId, @Param("tenantId") String tenantId);

    @Select({
            "<script>",
            "SELECT DISTINCT ut.tenant_id",
            "FROM nscp_sys_user_tenant ut",
            "WHERE ut.user_id = #{userId}",
            "<if test='tenantIds != null and tenantIds.size() > 0'>",
            "AND ut.tenant_id IN",
            "<foreach item='id' collection='tenantIds' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "</script>"
    })
    List<String> selectBoundTenantIds(
            @Param("userId") Long userId,
            @Param("tenantIds") List<String> tenantIds
    );

    @Update("""
            UPDATE nscp_sys_user_tenant
            SET is_default = 0
            WHERE tenant_id = #{tenantId}
            """)
    void clearDefaultByTenant(@Param("tenantId") String tenantId);

    @Update("""
            UPDATE nscp_sys_user_tenant
            SET is_default = #{isDefault}
            WHERE user_id = #{userId}
              AND tenant_id = #{tenantId}
            """)
    void updateDefaultFlag(
            @Param("userId") Long userId,
            @Param("tenantId") String tenantId,
            @Param("isDefault") Integer isDefault
    );

    record TenantUserBindRow(
            Long userId,
            String username,
            String realName,
            String userStatus,
            Integer isDefault
    ) {
    }
}
