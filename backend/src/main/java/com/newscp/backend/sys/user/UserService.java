package com.newscp.backend.sys.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.dept.entity.SysDept;
import com.newscp.backend.sys.dept.mapper.SysDeptMapper;
import com.newscp.backend.sys.dept.mapper.SysUserDeptMapper;
import com.newscp.backend.sys.role.entity.SysRole;
import com.newscp.backend.sys.role.mapper.SysRoleMapper;
import com.newscp.backend.sys.role.mapper.SysUserRoleMapper;
import com.newscp.backend.sys.user.dto.PasswordChangeDTO;
import com.newscp.backend.sys.user.dto.UserCreateDTO;
import com.newscp.backend.sys.user.dto.UserPageQueryDTO;
import com.newscp.backend.sys.user.dto.UserProfileUpdateDTO;
import com.newscp.backend.sys.user.dto.UserUpdateDTO;
import com.newscp.backend.sys.user.entity.SysUser;
import com.newscp.backend.sys.user.mapper.SysUserMapper;
import com.newscp.backend.sys.user.vo.ResetPasswordVO;
import com.newscp.backend.sys.user.vo.RoleBriefVO;
import com.newscp.backend.sys.user.vo.UserDetailVO;
import com.newscp.backend.sys.user.vo.UserVO;
import com.newscp.backend.tenant.TenantContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserDeptMapper userDeptMapper;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public UserService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            SysDeptMapper deptMapper,
            SysUserDeptMapper userDeptMapper,
            PasswordEncoder passwordEncoder,
            @Value("${sys.default.password:Admin@2026}") String defaultPassword
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.deptMapper = deptMapper;
        this.userDeptMapper = userDeptMapper;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    public PageResult<UserVO> page(UserPageQueryDTO query) {
        Page<SysUser> mpPage = new Page<>(query.safePage(), query.safeSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreatedAt);
        if (StringUtils.hasText(query.username())) {
            wrapper.like(SysUser::getUsername, query.username().trim());
        }
        if (StringUtils.hasText(query.realName())) {
            wrapper.like(SysUser::getRealName, query.realName().trim());
        }
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(SysUser::getStatus, query.status().trim());
        }

        Page<SysUser> result = userMapper.selectPage(mpPage, wrapper);
        List<SysUser> users = result.getRecords();
        List<Long> userIds = users.stream().map(SysUser::getId).toList();

        Map<Long, SysUserDeptMapper.UserDeptRow> deptMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userDeptMapper.selectUserDeptRows(userIds).forEach(row -> deptMap.put(row.userId(), row));
        }

        Map<Long, List<RoleBriefVO>> roleMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUserRoleMapper.UserRoleBindRow row : userRoleMapper.selectRoleRowsByUserIds(userIds)) {
                roleMap.computeIfAbsent(row.userId(), k -> new ArrayList<>())
                        .add(new RoleBriefVO(row.roleId(), row.roleName()));
            }
        }

        List<UserVO> records = users.stream().map(user -> {
            SysUserDeptMapper.UserDeptRow deptRow = deptMap.get(user.getId());
            return new UserVO(
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getUserType(),
                    user.getStatus(),
                    deptRow == null ? null : deptRow.deptId(),
                    deptRow == null ? null : deptRow.deptName(),
                    roleMap.getOrDefault(user.getId(), List.of()),
                    user.getCreatedAt()
            );
        }).toList();

        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), records);
    }

    @Transactional
    public Long create(UserCreateDTO dto) {
        validateUserCreate(dto);
        SysUser user = new SysUser();
        user.setUsername(dto.username().trim());
        user.setTenantId(TenantContext.getTenantId());
        user.setRealName(dto.realName().trim());
        user.setEmail(normalize(dto.email()));
        user.setPhone(normalize(dto.phone()));
        user.setUserType(StringUtils.hasText(dto.userType()) ? dto.userType().trim() : "NORMAL");
        user.setStatus("ACTIVE");
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setPwdChangedAt(LocalDateTime.now());
        user.setLoginFailCount(0);
        user.setCreatedBy(SecurityUtils.getCurrentUserId());
        user.setCreatedAt(LocalDateTime.now());
        user.setDeleted(0);
        userMapper.insertAutoId(user);

        replaceRoles(user.getId(), dto.roleIds());
        replaceDept(user.getId(), dto.deptId());
        return user.getId();
    }

    public UserDetailVO detail(Long id) {
        SysUser user = getUserOrThrow(id);
        Long deptId = userDeptMapper.selectPrimaryDeptIdByUserId(id);
        String deptName = null;
        if (deptId != null) {
            SysDept dept = deptMapper.selectById(deptId);
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
        return new UserDetailVO(
                user.getId(),
                user.getTenantId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getUserType(),
                user.getStatus(),
                user.getLockedUntil(),
                user.getPwdChangedAt(),
                deptId,
                deptName,
                roleIds,
                user.getCreatedAt()
        );
    }

    @Transactional
    public void update(Long id, UserUpdateDTO dto) {
        SysUser user = getUserOrThrow(id);
        validateRoles(dto.roleIds());
        validateDept(dto.deptId());
        user.setRealName(dto.realName().trim());
        user.setEmail(normalize(dto.email()));
        user.setPhone(normalize(dto.phone()));
        user.setUserType(StringUtils.hasText(dto.userType()) ? dto.userType().trim() : "NORMAL");
        user.setUpdatedBy(SecurityUtils.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        replaceRoles(id, dto.roleIds());
        replaceDept(id, dto.deptId());
    }

    @Transactional
    public void delete(Long id) {
        getUserOrThrow(id);
        userRoleMapper.deleteByUserId(id);
        userDeptMapper.deleteByUserId(id);
        userMapper.deleteById(id);
    }

    @Transactional
    public void enable(Long id) {
        SysUser user = getUserOrThrow(id);
        user.setStatus("ACTIVE");
        user.setLockedUntil(null);
        user.setLoginFailCount(0);
        user.setUpdatedBy(SecurityUtils.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public void disable(Long id) {
        SysUser user = getUserOrThrow(id);
        user.setStatus("DISABLED");
        user.setUpdatedBy(SecurityUtils.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public ResetPasswordVO resetPassword(Long id) {
        getUserOrThrow(id);
        userMapper.updatePassword(id, passwordEncoder.encode(defaultPassword), LocalDateTime.now());
        return new ResetPasswordVO(defaultPassword);
    }

    public UserDetailVO me() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        return detail(userId);
    }

    @Transactional
    public void updateProfile(UserProfileUpdateDTO dto) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        SysUser user = getUserOrThrow(userId);
        user.setRealName(dto.realName().trim());
        user.setEmail(normalize(dto.email()));
        user.setPhone(normalize(dto.phone()));
        user.setAvatarUrl(normalize(dto.avatarUrl()));
        user.setUpdatedBy(SecurityUtils.getCurrentUserId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public void changePassword(PasswordChangeDTO dto) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        SysUser user = getUserOrThrow(userId);
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        if (dto.newPassword().equals(dto.oldPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new BusinessException("新密码与确认密码不一致");
        }
        if (!isPasswordStrong(dto.newPassword())) {
            throw new BusinessException("新密码复杂度不足（至少8位，含大小写字母、数字和特殊字符）");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(dto.newPassword()), LocalDateTime.now());
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void validateUserCreate(UserCreateDTO dto) {
        if (existsUsername(dto.username().trim())) {
            throw new BusinessException("用户名已存在");
        }
        validateRoles(dto.roleIds());
        validateDept(dto.deptId());
    }

    private boolean existsUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0);
        return userMapper.selectCount(wrapper) > 0;
    }

    private void validateRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<Long> ids = roleIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long roleId : ids) {
            SysRole role = roleMapper.selectById(roleId);
            if (role == null || Integer.valueOf(1).equals(role.getDeleted())) {
                throw new BusinessException("角色不存在: " + roleId);
            }
        }
    }

    private void validateDept(Long deptId) {
        if (deptId == null) {
            return;
        }
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null || Integer.valueOf(1).equals(dept.getDeleted())) {
            throw new BusinessException("部门不存在: " + deptId);
        }
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds.stream().filter(Objects::nonNull).distinct().toList()) {
            userRoleMapper.insert(userId, roleId);
        }
    }

    private void replaceDept(Long userId, Long deptId) {
        userDeptMapper.deleteByUserId(userId);
        if (deptId == null) {
            return;
        }
        userDeptMapper.insert(userId, deptId, 1);
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isPasswordStrong(String value) {
        return value != null
                && value.length() >= 8
                && value.matches(".*[a-z].*")
                && value.matches(".*[A-Z].*")
                && value.matches(".*\\d.*")
                && value.matches(".*[^A-Za-z0-9].*");
    }
}
