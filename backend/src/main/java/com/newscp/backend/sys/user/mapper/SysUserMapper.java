package com.newscp.backend.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newscp.backend.sys.user.entity.SysUser;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Insert("""
            INSERT INTO nscp_sys_user(
                tenant_id, username, password_hash, real_name, email, phone, avatar_url,
                user_type, status, locked_until, pwd_changed_at, login_fail_count, last_login_at,
                created_by, created_at, updated_by, updated_at, deleted
            ) VALUES (
                #{tenantId}, #{username}, #{passwordHash}, #{realName}, #{email}, #{phone}, #{avatarUrl},
                #{userType}, #{status}, #{lockedUntil}, #{pwdChangedAt}, #{loginFailCount}, #{lastLoginAt},
                #{createdBy}, #{createdAt}, #{updatedBy}, #{updatedAt}, #{deleted}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertAutoId(SysUser user);

    @Select("""
            SELECT *
            FROM nscp_sys_user
            WHERE tenant_id = #{tenantId}
              AND username = #{username}
              AND deleted = 0
            LIMIT 1
            """)
    SysUser selectByTenantAndUsername(@Param("tenantId") String tenantId, @Param("username") String username);

    @Update("""
            UPDATE nscp_sys_user
            SET login_fail_count = #{failCount},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    void incrementFailCount(@Param("id") Long id, @Param("failCount") int failCount);

    @Update("""
            UPDATE nscp_sys_user
            SET status = 'LOCKED',
                locked_until = #{lockedUntil},
                login_fail_count = #{failCount},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    void lockUser(@Param("id") Long id, @Param("lockedUntil") LocalDateTime lockedUntil, @Param("failCount") int failCount);

    @Update("""
            UPDATE nscp_sys_user
            SET status = 'ACTIVE',
                locked_until = NULL,
                login_fail_count = 0,
                updated_at = NOW()
            WHERE id = #{id}
            """)
    void resetLock(@Param("id") Long id);

    @Update("""
            UPDATE nscp_sys_user
            SET login_fail_count = 0,
                status = 'ACTIVE',
                locked_until = NULL,
                last_login_at = #{lastLoginAt},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    void recordLoginSuccess(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Update("""
            UPDATE nscp_sys_user
            SET password_hash = #{passwordHash},
                pwd_changed_at = #{pwdChangedAt},
                updated_at = NOW()
            WHERE id = #{id}
            """)
    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash, @Param("pwdChangedAt") LocalDateTime pwdChangedAt);
}
