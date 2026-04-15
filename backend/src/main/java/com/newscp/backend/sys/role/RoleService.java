package com.newscp.backend.sys.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.role.dto.RoleAssignPermDTO;
import com.newscp.backend.sys.role.dto.RoleCreateDTO;
import com.newscp.backend.sys.role.dto.RolePageQueryDTO;
import com.newscp.backend.sys.role.dto.RoleUpdateDTO;
import com.newscp.backend.sys.role.entity.SysPermission;
import com.newscp.backend.sys.role.entity.SysRole;
import com.newscp.backend.sys.role.mapper.SysPermissionMapper;
import com.newscp.backend.sys.role.mapper.SysRoleMapper;
import com.newscp.backend.sys.role.mapper.SysRolePermissionMapper;
import com.newscp.backend.sys.role.mapper.SysUserRoleMapper;
import com.newscp.backend.sys.role.vo.PermissionTreeVO;
import com.newscp.backend.sys.role.vo.RoleDetailVO;
import com.newscp.backend.sys.role.vo.RoleOptionVO;
import com.newscp.backend.sys.role.vo.RoleVO;
import com.newscp.backend.tenant.TenantContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysPermissionMapper permissionMapper;

    public RoleService(
            SysRoleMapper roleMapper,
            SysRolePermissionMapper rolePermissionMapper,
            SysUserRoleMapper userRoleMapper,
            SysPermissionMapper permissionMapper
    ) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionMapper = permissionMapper;
    }

    public PageResult<RoleVO> page(RolePageQueryDTO query) {
        Page<SysRole> mpPage = new Page<>(query.safePage(), query.safeSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getSortOrder, SysRole::getId);
        if (StringUtils.hasText(query.roleName())) {
            wrapper.like(SysRole::getRoleName, query.roleName().trim());
        }
        Page<SysRole> result = roleMapper.selectPage(mpPage, wrapper);
        List<RoleVO> records = result.getRecords().stream()
                .map(r -> new RoleVO(
                        r.getId(),
                        r.getRoleCode(),
                        r.getRoleName(),
                        r.getRemark(),
                        r.getSortOrder(),
                        roleMapper.countUsersByRoleId(r.getId())
                ))
                .toList();
        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), records);
    }

    @Transactional
    public Long create(RoleCreateDTO dto) {
        if (existsRoleCode(dto.roleCode(), null)) {
            throw new BusinessException("角色编码已存在");
        }
        validatePermissionScope(dto.permIds());

        SysRole role = new SysRole();
        role.setRoleCode(dto.roleCode().trim());
        role.setRoleName(dto.roleName().trim());
        role.setRemark(dto.remark());
        role.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        role.setTenantId(TenantContext.getTenantId());
        role.setCreatedBy(SecurityUtils.getCurrentUserId());
        role.setCreatedAt(LocalDateTime.now());
        role.setDeleted(0);
        roleMapper.insert(role);

        replacePermissions(role.getId(), dto.permIds());
        return role.getId();
    }

    public RoleDetailVO detail(Long id) {
        SysRole role = getRoleOrThrow(id);
        List<Long> permIds = rolePermissionMapper.selectPermIdsByRoleId(id);
        return new RoleDetailVO(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getRemark(),
                role.getSortOrder(),
                permIds
        );
    }

    @Transactional
    public void update(Long id, RoleUpdateDTO dto) {
        SysRole role = getRoleOrThrow(id);
        validatePermissionScope(dto.permIds());

        role.setRoleName(dto.roleName().trim());
        role.setRemark(dto.remark());
        role.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        role.setUpdatedBy(SecurityUtils.getCurrentUserId());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);

        if (dto.permIds() != null) {
            replacePermissions(id, dto.permIds());
        }
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = getRoleOrThrow(id);
        if ("TENANT_ADMIN_ROLE".equalsIgnoreCase(role.getRoleCode())) {
            throw new BusinessException("内置系统角色禁止删除");
        }
        long boundUsers = roleMapper.countUsersByRoleId(id);
        if (boundUsers > 0) {
            throw new BusinessException("该角色下存在 " + boundUsers + " 个用户，请先解除绑定再删除");
        }
        rolePermissionMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);
    }

    public List<RoleOptionVO> listAll() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0)
                .orderByAsc(SysRole::getSortOrder, SysRole::getId));
        return roles.stream().map(r -> new RoleOptionVO(r.getId(), r.getRoleCode(), r.getRoleName())).toList();
    }

    @Transactional
    public void assignPermissions(Long roleId, RoleAssignPermDTO dto) {
        getRoleOrThrow(roleId);
        validatePermissionScope(dto.permIds());
        replacePermissions(roleId, dto.permIds());
    }

    public List<PermissionTreeVO> permissionTree() {
        List<SysPermission> perms = permissionMapper.selectAllEnabled();
        Map<Long, PermissionTreeVO> map = new HashMap<>();
        for (SysPermission p : perms) {
            PermissionTreeVO node = new PermissionTreeVO();
            node.setId(p.getId());
            node.setParentId(p.getParentId());
            node.setPermCode(p.getPermCode());
            node.setPermName(p.getPermName());
            node.setPermType(p.getPermType());
            node.setRoutePath(p.getRoutePath());
            node.setComponentPath(p.getComponentPath());
            node.setIcon(p.getIcon());
            node.setSortOrder(p.getSortOrder());
            node.setIsHidden(p.getIsHidden());
            node.setStatus(p.getStatus());
            map.put(p.getId(), node);
        }

        List<PermissionTreeVO> roots = new ArrayList<>();
        for (SysPermission p : perms) {
            PermissionTreeVO node = map.get(p.getId());
            if (p.getParentId() == null || p.getParentId() == 0 || !map.containsKey(p.getParentId())) {
                roots.add(node);
            } else {
                map.get(p.getParentId()).getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<PermissionTreeVO> nodes) {
        nodes.sort(Comparator.comparing(PermissionTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PermissionTreeVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (PermissionTreeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || Integer.valueOf(1).equals(role.getDeleted())) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    private boolean existsRoleCode(String roleCode, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode.trim())
                .eq(SysRole::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        return roleMapper.selectCount(wrapper) > 0;
    }

    private void replacePermissions(Long roleId, List<Long> permIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permIds == null || permIds.isEmpty()) {
            return;
        }
        for (Long permId : permIds.stream().filter(Objects::nonNull).distinct().toList()) {
            rolePermissionMapper.insert(roleId, permId);
        }
    }

    private void validatePermissionScope(List<Long> permIds) {
        if (permIds == null || permIds.isEmpty()) {
            return;
        }
        Long operatorId = Long.parseLong(SecurityUtils.getCurrentUserId());
        Set<Long> selfPermIds = permissionMapper.selectByUserId(operatorId)
                .stream()
                .map(SysPermission::getId)
                .collect(Collectors.toSet());
        List<Long> illegal = permIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !selfPermIds.contains(id))
                .toList();
        if (!illegal.isEmpty()) {
            throw new BusinessException("存在超出当前用户权限范围的权限分配");
        }
    }
}
