package com.newscp.backend.auth;

import com.newscp.backend.auth.dto.LoginRequest;
import com.newscp.backend.auth.dto.LoginResponse;
import com.newscp.backend.auth.dto.MeResponse;
import com.newscp.backend.auth.dto.ForceChangePasswordRequest;
import com.newscp.backend.auth.jwt.JwtTokenProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.sys.security.PasswordPolicyService;
import com.newscp.backend.sys.security.entity.PasswordPolicy;
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

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysTenantMapper tenantMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final PasswordPolicyService passwordPolicyService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysPermissionMapper permissionMapper,
            SysTenantMapper tenantMapper,
            SysUserTenantMapper userTenantMapper,
            PasswordPolicyService passwordPolicyService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.tenantMapper = tenantMapper;
        this.userTenantMapper = userTenantMapper;
        this.passwordPolicyService = passwordPolicyService;
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

        PasswordPolicy policy = passwordPolicyService.getEffective(tenantId);
        checkUserStatus(user, policy);

        int failCountBefore = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleLoginFailure(user, policy);
            int threshold = passwordPolicyService.getLockThreshold(policy);
            if (threshold == Integer.MAX_VALUE) {
                throw new BusinessException("用户名或密码错误");
            }
            int remaining = threshold - (failCountBefore + 1);
            if (remaining <= 0) {
                int lockMinutes = passwordPolicyService.getLockDuration(policy);
                throw new BusinessException("用户名或密码错误，账号已被锁定，请 " + lockMinutes + " 分钟后重试");
            }
            throw new BusinessException("用户名或密码错误，还剩 " + remaining + " 次机会");
        }

        String expiredMsg = passwordPolicyService.checkExpired(user, policy);
        if (expiredMsg != null) {
            throw new BusinessException(600, expiredMsg);
        }

        String forceMsg = passwordPolicyService.checkForceChange(user, request.password(), policy);
        if (forceMsg != null) {
            throw new BusinessException(600, forceMsg);
        }

        handleLoginSuccess(user);
        String pwdExpireWarning = passwordPolicyService.getExpirationWarning(user, policy);

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
                roles,
                pwdExpireWarning
        );
    }

    @Transactional
    public void forceChangePassword(ForceChangePasswordRequest request) {
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
            throw new BusinessException("用户不存在");
        }
        validateTenantLoginScope(user, tenantId);
        PasswordPolicy policy = passwordPolicyService.getEffective(tenantId);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("新密码与确认密码不一致");
        }
        if (request.newPassword().equals(request.oldPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        List<String> errors = passwordPolicyService.validatePassword(request.newPassword(), policy);
        if (!errors.isEmpty()) {
            throw new BusinessException("新密码不符合策略：" + String.join("；", errors));
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.newPassword()), LocalDateTime.now());
        userMapper.resetLock(user.getId());
        userMapper.recordLoginSuccess(user.getId(), LocalDateTime.now());
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

    private void checkUserStatus(SysUser user, PasswordPolicy policy) {
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已禁用，请联系管理员");
        }
        if ("LOCKED".equals(user.getStatus())) {
            boolean autoUnlock = passwordPolicyService.isAutoUnlock(policy);
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()) && autoUnlock) {
                long minutes = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
                throw new BusinessException("账号已锁定，请 " + minutes + " 分钟后重试");
            }
            if (!autoUnlock) {
                throw new BusinessException("账号已锁定，请联系管理员解锁");
            }
            userMapper.resetLock(user.getId());
            user.setStatus("ACTIVE");
            user.setLoginFailCount(0);
        }
    }

    private void handleLoginFailure(SysUser user, PasswordPolicy policy) {
        if (passwordPolicyService.getLockDuration(policy) <= 0
                || passwordPolicyService.getLockThreshold(policy) == Integer.MAX_VALUE) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            userMapper.incrementFailCount(user.getId(), failCount);
            return;
        }
        int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        int lockThreshold = passwordPolicyService.getLockThreshold(policy);
        if (failCount >= lockThreshold) {
            int lockMinutes = passwordPolicyService.getLockDuration(policy);
            userMapper.lockUser(user.getId(), LocalDateTime.now().plusMinutes(lockMinutes), failCount);
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
