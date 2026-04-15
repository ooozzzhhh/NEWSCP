package com.newscp.backend.auth;

import com.newscp.backend.auth.dto.LoginRequest;
import com.newscp.backend.auth.dto.LoginResponse;
import com.newscp.backend.auth.dto.MeResponse;
import com.newscp.backend.auth.jwt.JwtTokenProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.sys.role.mapper.SysPermissionMapper;
import com.newscp.backend.sys.role.mapper.SysRoleMapper;
import com.newscp.backend.sys.user.entity.SysUser;
import com.newscp.backend.sys.user.mapper.SysUserMapper;
import com.newscp.backend.tenant.entity.SysTenant;
import com.newscp.backend.tenant.mapper.SysTenantMapper;
import com.newscp.backend.tenant.mapper.SysUserTenantMapper;
import com.newscp.backend.tenant.TenantContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final String DEFAULT_TENANT_ID = "admin";
    private static final int LOCK_THRESHOLD = 5;
    private static final int LOCK_MINUTES = 30;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysPermissionMapper permissionMapper,
            SysTenantMapper tenantMapper,
            SysUserTenantMapper userTenantMapper,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.tenantMapper = tenantMapper;
        this.userTenantMapper = userTenantMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String tenantId = StringUtils.hasText(request.tenantId())
                ? request.tenantId().trim()
                : DEFAULT_TENANT_ID;

        TenantContext.setTenantId(tenantId);
        SysUser user;
        try {
            user = userMapper.selectByTenantAndUsername(tenantId, request.username());
        } finally {
            TenantContext.clear();
        }

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        validateTenantLoginScope(user, tenantId);

        checkUserStatus(user);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleLoginFailure(user);
            int remaining = LOCK_THRESHOLD - (user.getLoginFailCount() + 1);
            if (remaining <= 0) {
                throw new BusinessException("用户名或密码错误，账号已被锁定，请 " + LOCK_MINUTES + " 分钟后重试");
            }
            throw new BusinessException("用户名或密码错误，还剩 " + remaining + " 次机会");
        }

        handleLoginSuccess(user);

        List<String> roles = roleMapper.selectRoleCodesByUserIdIgnoreTenant(user.getId());

        String token = jwtTokenProvider.generateToken(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getRealName(),
                tenantId,
                roles
        );
        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getRealName(),
                tenantId,
                roles
        );
    }

    public MeResponse getMe(String userId) {
        SysUser user = userMapper.selectById(Long.parseLong(userId));
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        List<String> roleCodes = roleMapper.selectRoleCodesByUserIdIgnoreTenant(user.getId());
        List<String> permCodes = permissionMapper.selectPermCodesByUserId(user.getId());
        return new MeResponse(
                userId,
                user.getUsername(),
                user.getRealName(),
                user.getTenantId(),
                roleCodes,
                permCodes
        );
    }

    private void checkUserStatus(SysUser user) {
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已禁用，请联系管理员");
        }
        if ("LOCKED".equals(user.getStatus())) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                long minutes = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
                throw new BusinessException("账号已锁定，请 " + minutes + " 分钟后重试");
            }
            userMapper.resetLock(user.getId());
            user.setStatus("ACTIVE");
            user.setLoginFailCount(0);
        }
    }

    private void handleLoginFailure(SysUser user) {
        int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        if (failCount >= LOCK_THRESHOLD) {
            userMapper.lockUser(user.getId(), LocalDateTime.now().plusMinutes(LOCK_MINUTES), failCount);
            return;
        }
        userMapper.incrementFailCount(user.getId(), failCount);
    }

    private void handleLoginSuccess(SysUser user) {
        userMapper.recordLoginSuccess(user.getId(), LocalDateTime.now());
    }

    private void validateTenantLoginScope(SysUser user, String tenantId) {
        SysTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantId, tenantId)
                .eq(SysTenant::getDeleted, 0)
                .last("LIMIT 1"));
        if (tenant == null) {
            throw new BusinessException("租户不存在");
        }
        if (!"ENABLED".equalsIgnoreCase(tenant.getStatus())) {
            throw new BusinessException("租户已停用，请联系管理员");
        }
        if (tenant.getExpireAt() != null && tenant.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("租户已过期，请联系管理员续期");
        }
        long bindCount = userTenantMapper.countBind(user.getId(), tenantId);
        if (bindCount <= 0) {
            throw new BusinessException("当前用户未开通该租户访问权限");
        }
        List<String> roles = roleMapper.selectRoleCodesByUserIdIgnoreTenant(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException("当前用户未分配角色，无法登录");
        }
    }
}
