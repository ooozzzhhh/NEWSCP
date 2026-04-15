package com.newscp.backend.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.user.entity.SysUser;
import com.newscp.backend.sys.user.mapper.SysUserMapper;
import com.newscp.backend.tenant.dto.TenantCreateDTO;
import com.newscp.backend.tenant.dto.TenantPageQueryDTO;
import com.newscp.backend.tenant.dto.TenantUpdateDTO;
import com.newscp.backend.tenant.dto.TenantUserAssignDTO;
import com.newscp.backend.tenant.entity.SysTenant;
import com.newscp.backend.tenant.mapper.SysTenantMapper;
import com.newscp.backend.tenant.mapper.SysUserTenantMapper;
import com.newscp.backend.tenant.vo.TenantUserVO;
import com.newscp.backend.tenant.vo.TenantVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TenantService {

    private static final Set<String> VALID_STATUS = Set.of("ENABLED", "DISABLED");
    private static final String SUPER_TENANT_ID = "admin";
    private static final Long SUPER_ADMIN_USER_ID = 1L;

    private final SysTenantMapper tenantMapper;
    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;

    public TenantService(
            SysTenantMapper tenantMapper,
            SysUserMapper userMapper,
            SysUserTenantMapper userTenantMapper
    ) {
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.userTenantMapper = userTenantMapper;
    }

    public PageResult<TenantVO> page(TenantPageQueryDTO query) {
        Page<SysTenant> mpPage = new Page<>(query.safePage(), query.safeSize());
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getDeleted, 0)
                .orderByDesc(SysTenant::getCreatedAt);
        if (StringUtils.hasText(query.keyword())) {
            String keyword = query.keyword().trim();
            wrapper.and(w -> w.like(SysTenant::getTenantId, keyword).or().like(SysTenant::getTenantName, keyword));
        }
        if (StringUtils.hasText(query.status())) {
            wrapper.eq(SysTenant::getStatus, query.status().trim());
        }
        Page<SysTenant> result = tenantMapper.selectPage(mpPage, wrapper);
        List<TenantVO> records = result.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), records);
    }

    public TenantVO detail(Long id) {
        return toVO(getTenantOrThrow(id));
    }

    public List<TenantUserVO> listUsers(Long id) {
        SysTenant tenant = getTenantOrThrow(id);
        return userTenantMapper.selectUsersByTenantId(tenant.getTenantId()).stream()
                .map(row -> new TenantUserVO(
                        row.userId(),
                        row.username(),
                        row.realName(),
                        row.userStatus(),
                        row.isDefault() != null && row.isDefault() == 1
                ))
                .toList();
    }

    @Transactional
    public Long create(TenantCreateDTO dto) {
        String tenantId = normalizeTenantId(dto.tenantId());
        if (existsTenantId(tenantId)) {
            throw new BusinessException("租户ID已存在");
        }
        SysTenant tenant = new SysTenant();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(normalizeRequired(dto.tenantName(), "租户名称不能为空"));
        tenant.setStatus(normalizeStatus(dto.status()));
        tenant.setExpireAt(dto.expireAt());
        tenant.setContactName(normalizeNullable(dto.contactName()));
        tenant.setContactPhone(normalizeNullable(dto.contactPhone()));
        tenant.setContactEmail(normalizeNullable(dto.contactEmail()));
        tenant.setRemark(normalizeNullable(dto.remark()));
        tenant.setCreatedBy(SecurityUtils.getCurrentUserId());
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setDeleted(0);
        tenantMapper.insert(tenant);

        replaceTenantUsers(tenantId, dto.userIds(), dto.defaultUserId());
        return tenant.getId();
    }

    @Transactional
    public void update(Long id, TenantUpdateDTO dto) {
        SysTenant tenant = getTenantOrThrow(id);
        tenant.setTenantName(normalizeRequired(dto.tenantName(), "租户名称不能为空"));
        tenant.setStatus(normalizeStatus(dto.status()));
        tenant.setExpireAt(dto.expireAt());
        tenant.setContactName(normalizeNullable(dto.contactName()));
        tenant.setContactPhone(normalizeNullable(dto.contactPhone()));
        tenant.setContactEmail(normalizeNullable(dto.contactEmail()));
        tenant.setRemark(normalizeNullable(dto.remark()));
        tenant.setUpdatedBy(SecurityUtils.getCurrentUserId());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);

        replaceTenantUsers(tenant.getTenantId(), dto.userIds(), dto.defaultUserId());
    }

    @Transactional
    public void assignUsers(Long id, TenantUserAssignDTO dto) {
        SysTenant tenant = getTenantOrThrow(id);
        replaceTenantUsers(tenant.getTenantId(), dto.userIds(), dto.defaultUserId());
    }

    @Transactional
    public void delete(Long id) {
        SysTenant tenant = getTenantOrThrow(id);
        if (SUPER_TENANT_ID.equals(tenant.getTenantId())) {
            throw new BusinessException("超级管理员租户禁止删除");
        }
        long userCount = userTenantMapper.countUsersByTenantId(tenant.getTenantId());
        if (userCount > 0) {
            throw new BusinessException("该租户下存在 " + userCount + " 个绑定用户，请先解除绑定");
        }
        tenantMapper.deleteById(id);
    }

    private void replaceTenantUsers(String tenantId, List<Long> userIds, Long defaultUserId) {
        List<Long> normalizedUserIds = normalizeUserIds(userIds);
        validateUserIds(normalizedUserIds);
        Long finalDefaultUserId = chooseDefaultUserId(tenantId, normalizedUserIds, defaultUserId);

        userTenantMapper.deleteByTenantId(tenantId);
        if (normalizedUserIds.isEmpty()) {
            return;
        }
        String operator = SecurityUtils.getCurrentUserId();
        for (Long userId : normalizedUserIds) {
            int isDefault = finalDefaultUserId != null && finalDefaultUserId.equals(userId) ? 1 : 0;
            userTenantMapper.insert(userId, tenantId, isDefault, operator);
        }
    }

    private Long chooseDefaultUserId(String tenantId, List<Long> userIds, Long defaultUserId) {
        if (userIds.isEmpty()) {
            return null;
        }
        if (defaultUserId != null) {
            if (!userIds.contains(defaultUserId)) {
                throw new BusinessException("默认用户必须在租户成员列表中");
            }
            return defaultUserId;
        }
        if (SUPER_TENANT_ID.equals(tenantId) && userIds.contains(SUPER_ADMIN_USER_ID)) {
            return SUPER_ADMIN_USER_ID;
        }
        return userIds.get(0);
    }

    private List<Long> normalizeUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void validateUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Set<Long> existed = users.stream()
                .filter(u -> !Integer.valueOf(1).equals(u.getDeleted()))
                .map(SysUser::getId)
                .collect(Collectors.toSet());
        List<Long> illegal = userIds.stream().filter(id -> !existed.contains(id)).toList();
        if (!illegal.isEmpty()) {
            throw new BusinessException("用户不存在: " + illegal);
        }
    }

    private boolean existsTenantId(String tenantId) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantId, tenantId)
                .eq(SysTenant::getDeleted, 0);
        return tenantMapper.selectCount(wrapper) > 0;
    }

    private SysTenant getTenantOrThrow(Long id) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant == null || Integer.valueOf(1).equals(tenant.getDeleted())) {
            throw new BusinessException("租户不存在");
        }
        return tenant;
    }

    private TenantVO toVO(SysTenant tenant) {
        return new TenantVO(
                tenant.getId(),
                tenant.getTenantId(),
                tenant.getTenantName(),
                tenant.getStatus(),
                tenant.getExpireAt(),
                tenant.getContactName(),
                tenant.getContactPhone(),
                tenant.getContactEmail(),
                tenant.getRemark(),
                userTenantMapper.countUsersByTenantId(tenant.getTenantId()),
                tenant.getCreatedAt()
        );
    }

    private String normalizeTenantId(String value) {
        String normalized = normalizeRequired(value, "租户ID不能为空");
        if (!normalized.matches("^[A-Za-z0-9_-]{3,64}$")) {
            throw new BusinessException("租户ID格式非法（仅支持字母、数字、下划线、中划线，长度3-64）");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String errorMsg) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(errorMsg);
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String value) {
        String status = StringUtils.hasText(value) ? value.trim().toUpperCase() : "ENABLED";
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException("租户状态仅支持 ENABLED 或 DISABLED");
        }
        return status;
    }
}
