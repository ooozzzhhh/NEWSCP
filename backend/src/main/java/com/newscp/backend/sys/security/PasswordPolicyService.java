package com.newscp.backend.sys.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.security.dto.PasswordPolicyDTO;
import com.newscp.backend.sys.security.entity.PasswordPolicy;
import com.newscp.backend.sys.security.mapper.PasswordPolicyMapper;
import com.newscp.backend.sys.security.vo.PasswordPolicyVO;
import com.newscp.backend.sys.user.entity.SysUser;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PasswordPolicyService {

    private final PasswordPolicyMapper passwordPolicyMapper;

    public PasswordPolicyService(PasswordPolicyMapper passwordPolicyMapper) {
        this.passwordPolicyMapper = passwordPolicyMapper;
    }

    @Transactional
    public PasswordPolicyVO getCurrentPolicy(String tenantId) {
        return toVO(getEffective(tenantId));
    }

    @Transactional
    public PasswordPolicy getEffective(String tenantId) {
        PasswordPolicy policy = passwordPolicyMapper.selectOne(new LambdaQueryWrapper<PasswordPolicy>()
                .eq(PasswordPolicy::getTenantId, tenantId)
                .last("LIMIT 1"));
        if (policy != null) {
            return policy;
        }
        PasswordPolicy defaults = createDefaultPolicy(tenantId);
        passwordPolicyMapper.insert(defaults);
        return defaults;
    }

    @Transactional
    public void update(String tenantId, PasswordPolicyDTO dto) {
        PasswordPolicy current = getEffective(tenantId);
        validatePolicy(dto);
        PasswordPolicy candidate = apply(dto, current);
        List<String> defaultPasswordErrors = validatePassword(candidate.getDefaultPassword(), candidate);
        if (!defaultPasswordErrors.isEmpty()) {
            throw new BusinessException("默认密码不符合当前策略：" + String.join("；", defaultPasswordErrors));
        }

        current.setMinLength(candidate.getMinLength());
        current.setMaxLength(candidate.getMaxLength());
        current.setRequireDigit(candidate.getRequireDigit());
        current.setRequireLower(candidate.getRequireLower());
        current.setRequireUpper(candidate.getRequireUpper());
        current.setRequireSpecial(candidate.getRequireSpecial());
        current.setExpireEnabled(candidate.getExpireEnabled());
        current.setExpireDays(candidate.getExpireDays());
        current.setAlertBeforeDays(candidate.getAlertBeforeDays());
        current.setForceChangeDefault(candidate.getForceChangeDefault());
        current.setForceChangeOnRuleUpdate(candidate.getForceChangeOnRuleUpdate());
        current.setLockEnabled(candidate.getLockEnabled());
        current.setLockThreshold(candidate.getLockThreshold());
        current.setLockDuration(candidate.getLockDuration());
        current.setAutoUnlock(candidate.getAutoUnlock());
        current.setDefaultPassword(candidate.getDefaultPassword());
        current.setUpdatedBy(SecurityUtils.getCurrentUserId());
        current.setUpdatedAt(LocalDateTime.now());
        passwordPolicyMapper.updateById(current);
    }

    public List<String> validatePassword(String password, PasswordPolicy policy) {
        List<String> errors = new ArrayList<>();
        if (!StringUtils.hasText(password)) {
            errors.add("密码不能为空");
            return errors;
        }
        if (password.length() < safe(policy.getMinLength(), 8)) {
            errors.add("密码长度不能少于" + safe(policy.getMinLength(), 8) + "位");
        }
        if (password.length() > safe(policy.getMaxLength(), 32)) {
            errors.add("密码长度不能超过" + safe(policy.getMaxLength(), 32) + "位");
        }
        if (isEnabled(policy.getRequireDigit()) && password.chars().noneMatch(Character::isDigit)) {
            errors.add("需要包含数字");
        }
        if (isEnabled(policy.getRequireLower()) && password.chars().noneMatch(Character::isLowerCase)) {
            errors.add("需要包含小写字母");
        }
        if (isEnabled(policy.getRequireUpper()) && password.chars().noneMatch(Character::isUpperCase)) {
            errors.add("需要包含大写字母");
        }
        if (isEnabled(policy.getRequireSpecial())
                && password.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) {
            errors.add("需要包含特殊符号");
        }
        return errors;
    }

    public String checkExpired(SysUser user, PasswordPolicy policy) {
        if (!isEnabled(policy.getExpireEnabled())) {
            return null;
        }
        if (user.getPwdChangedAt() == null) {
            return "当前密码已过期，请先修改密码";
        }
        LocalDateTime expireAt = user.getPwdChangedAt().plusDays(safe(policy.getExpireDays(), 90));
        if (!expireAt.isAfter(LocalDateTime.now())) {
            return "当前密码已过期，请先修改密码";
        }
        return null;
    }

    public String checkForceChange(SysUser user, String rawPassword, PasswordPolicy policy) {
        if (isEnabled(policy.getForceChangeDefault()) && user.getPwdChangedAt() == null) {
            if (rawPassword != null && rawPassword.equals(policy.getDefaultPassword())) {
                return "当前账号使用默认密码，需先修改密码";
            }
            return "当前账号首次登录，需先修改密码";
        }

        if (isEnabled(policy.getForceChangeOnRuleUpdate()) && user.getPwdChangedAt() != null) {
            List<String> errors = validatePassword(rawPassword, policy);
            if (!errors.isEmpty()) {
                return "当前密码不符合最新策略，请修改密码后继续";
            }
        }
        return null;
    }

    public String getExpirationWarning(SysUser user, PasswordPolicy policy) {
        if (!isEnabled(policy.getExpireEnabled())) {
            return null;
        }
        if (user.getPwdChangedAt() == null) {
            return null;
        }
        int alertDays = safe(policy.getAlertBeforeDays(), 7);
        if (alertDays <= 0) {
            return null;
        }
        LocalDateTime expireAt = user.getPwdChangedAt().plusDays(safe(policy.getExpireDays(), 90));
        long days = Duration.between(LocalDateTime.now(), expireAt).toDays();
        if (days >= 0 && days <= alertDays) {
            return "您的密码将在 " + days + " 天后过期，请及时修改";
        }
        return null;
    }

    public int getLockThreshold(PasswordPolicy policy) {
        if (!isEnabled(policy.getLockEnabled())) {
            return Integer.MAX_VALUE;
        }
        return safe(policy.getLockThreshold(), 5);
    }

    public int getLockDuration(PasswordPolicy policy) {
        if (!isEnabled(policy.getLockEnabled())) {
            return 0;
        }
        return Math.max(1, safe(policy.getLockDuration(), 30));
    }

    public boolean isAutoUnlock(PasswordPolicy policy) {
        return isEnabled(policy.getAutoUnlock());
    }

    public String getDefaultPassword(String tenantId) {
        return getEffective(tenantId).getDefaultPassword();
    }

    private void validatePolicy(PasswordPolicyDTO dto) {
        if (dto.minLength() > dto.maxLength()) {
            throw new BusinessException("最小长度不能大于最大长度");
        }
        int typeCount = bool(dto.requireDigit()) + bool(dto.requireLower()) + bool(dto.requireUpper()) + bool(dto.requireSpecial());
        if (typeCount < 1) {
            throw new BusinessException("密码复杂度要求至少开启一项");
        }
        validateBoolean(dto.requireDigit(), "requireDigit");
        validateBoolean(dto.requireLower(), "requireLower");
        validateBoolean(dto.requireUpper(), "requireUpper");
        validateBoolean(dto.requireSpecial(), "requireSpecial");
        validateBoolean(dto.expireEnabled(), "expireEnabled");
        validateBoolean(dto.forceChangeDefault(), "forceChangeDefault");
        validateBoolean(dto.forceChangeOnRuleUpdate(), "forceChangeOnRuleUpdate");
        validateBoolean(dto.lockEnabled(), "lockEnabled");
        validateBoolean(dto.autoUnlock(), "autoUnlock");
        if (!StringUtils.hasText(dto.defaultPassword())) {
            throw new BusinessException("默认密码不能为空");
        }
    }

    private void validateBoolean(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException(field + " 必须是0或1");
        }
    }

    private PasswordPolicy apply(PasswordPolicyDTO dto, PasswordPolicy base) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setId(base.getId());
        policy.setTenantId(base.getTenantId());
        policy.setMinLength(dto.minLength());
        policy.setMaxLength(dto.maxLength());
        policy.setRequireDigit(dto.requireDigit());
        policy.setRequireLower(dto.requireLower());
        policy.setRequireUpper(dto.requireUpper());
        policy.setRequireSpecial(dto.requireSpecial());
        policy.setExpireEnabled(dto.expireEnabled());
        policy.setExpireDays(dto.expireDays());
        policy.setAlertBeforeDays(dto.alertBeforeDays());
        policy.setForceChangeDefault(dto.forceChangeDefault());
        policy.setForceChangeOnRuleUpdate(dto.forceChangeOnRuleUpdate());
        policy.setLockEnabled(dto.lockEnabled());
        policy.setLockThreshold(dto.lockThreshold());
        policy.setLockDuration(dto.lockDuration());
        policy.setAutoUnlock(dto.autoUnlock());
        policy.setDefaultPassword(dto.defaultPassword().trim());
        return policy;
    }

    private PasswordPolicy createDefaultPolicy(String tenantId) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setTenantId(tenantId);
        policy.setMinLength(8);
        policy.setMaxLength(32);
        policy.setRequireDigit(1);
        policy.setRequireLower(1);
        policy.setRequireUpper(0);
        policy.setRequireSpecial(0);
        policy.setExpireEnabled(0);
        policy.setExpireDays(90);
        policy.setAlertBeforeDays(7);
        policy.setForceChangeDefault(1);
        policy.setForceChangeOnRuleUpdate(0);
        policy.setLockEnabled(1);
        policy.setLockThreshold(5);
        policy.setLockDuration(30);
        policy.setAutoUnlock(1);
        policy.setDefaultPassword("Admin@2026");
        policy.setCreatedBy("system");
        policy.setCreatedAt(LocalDateTime.now());
        return policy;
    }

    private PasswordPolicyVO toVO(PasswordPolicy policy) {
        return new PasswordPolicyVO(
                policy.getId(),
                policy.getTenantId(),
                policy.getMinLength(),
                policy.getMaxLength(),
                policy.getRequireDigit(),
                policy.getRequireLower(),
                policy.getRequireUpper(),
                policy.getRequireSpecial(),
                policy.getExpireEnabled(),
                policy.getExpireDays(),
                policy.getAlertBeforeDays(),
                policy.getForceChangeDefault(),
                policy.getForceChangeOnRuleUpdate(),
                policy.getLockEnabled(),
                policy.getLockThreshold(),
                policy.getLockDuration(),
                policy.getAutoUnlock(),
                policy.getDefaultPassword()
        );
    }

    private boolean isEnabled(Integer value) {
        return value != null && value == 1;
    }

    private int safe(Integer value, int defaults) {
        return value == null ? defaults : value;
    }

    private int bool(Integer value) {
        return isEnabled(value) ? 1 : 0;
    }
}
