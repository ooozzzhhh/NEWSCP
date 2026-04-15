package com.newscp.backend.sys.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.newscp.backend.common.exception.BusinessException;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.permission.dto.PermissionCreateDTO;
import com.newscp.backend.sys.permission.dto.PermissionUpdateDTO;
import com.newscp.backend.sys.role.entity.SysPermission;
import com.newscp.backend.sys.role.mapper.SysPermissionMapper;
import com.newscp.backend.sys.role.vo.PermissionTreeVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PermissionOperateService {

    private static final Set<String> VALID_TYPES = Set.of("MENU", "BUTTON");
    private static final Set<String> VALID_STATUS = Set.of("ENABLED", "DISABLED");

    private final SysPermissionMapper permissionMapper;

    public PermissionOperateService(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public List<PermissionTreeVO> tree() {
        List<SysPermission> perms = permissionMapper.selectAllIncludeDisabled();
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

    @Transactional
    public Long create(PermissionCreateDTO dto) {
        String permType = normalizeType(dto.permType());
        String permCode = normalizePermCode(dto.permCode());
        if (existsPermCode(permCode, null)) {
            throw new BusinessException("权限编码已存在");
        }
        validateParent(dto.parentId(), null, permType);

        SysPermission permission = new SysPermission();
        permission.setParentId(dto.parentId());
        permission.setPermType(permType);
        permission.setPermCode(permCode);
        permission.setPermName(normalizeRequired(dto.permName(), "权限名称不能为空"));
        permission.setRoutePath(normalizeNullable(dto.routePath()));
        permission.setComponentPath(normalizeNullable(dto.componentPath()));
        permission.setIcon(normalizeNullable(dto.icon()));
        permission.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        permission.setIsHidden(dto.isHidden() == null ? 0 : dto.isHidden());
        permission.setStatus(normalizeStatus(dto.status()));
        permission.setCreatedBy(SecurityUtils.getCurrentUserId());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setDeleted(0);
        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Transactional
    public void update(Long id, PermissionUpdateDTO dto) {
        SysPermission permission = getPermissionOrThrow(id);
        validateParent(dto.parentId(), id, permission.getPermType());

        permission.setParentId(dto.parentId());
        permission.setPermName(normalizeRequired(dto.permName(), "权限名称不能为空"));
        permission.setRoutePath(normalizeNullable(dto.routePath()));
        permission.setComponentPath(normalizeNullable(dto.componentPath()));
        permission.setIcon(normalizeNullable(dto.icon()));
        permission.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        permission.setIsHidden(dto.isHidden() == null ? 0 : dto.isHidden());
        permission.setStatus(normalizeStatus(dto.status()));
        permission.setUpdatedBy(SecurityUtils.getCurrentUserId());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionMapper.updateById(permission);
    }

    @Transactional
    public void delete(Long id) {
        getPermissionOrThrow(id);
        long childCount = permissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, id)
                .eq(SysPermission::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException("存在子权限，请先删除子节点");
        }
        long roleBindCount = permissionMapper.countRoleBindByPermId(id);
        if (roleBindCount > 0) {
            throw new BusinessException("当前权限已被角色绑定，请先解除角色授权");
        }
        permissionMapper.deleteById(id);
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

    private void validateParent(Long parentId, Long selfId, String permType) {
        if (parentId == null) {
            throw new BusinessException("父节点不能为空");
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException("不能将自身设为父节点");
        }
        if (parentId == 0) {
            if ("BUTTON".equals(permType)) {
                throw new BusinessException("按钮权限必须挂在菜单下");
            }
            return;
        }
        SysPermission parent = permissionMapper.selectById(parentId);
        if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
            throw new BusinessException("父节点不存在");
        }
        if ("BUTTON".equals(parent.getPermType())) {
            throw new BusinessException("父节点不能是按钮");
        }

        if (selfId == null) {
            return;
        }
        Map<Long, Long> parentMap = permissionMapper.selectAllIncludeDisabled().stream()
                .collect(HashMap::new, (map, p) -> map.put(p.getId(), p.getParentId()), HashMap::putAll);
        Long cursor = parentId;
        while (cursor != null && cursor != 0) {
            if (selfId.equals(cursor)) {
                throw new BusinessException("不允许把节点移动到自己的子节点下");
            }
            cursor = parentMap.get(cursor);
        }
    }

    private SysPermission getPermissionOrThrow(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null || Integer.valueOf(1).equals(permission.getDeleted())) {
            throw new BusinessException("权限不存在");
        }
        return permission;
    }

    private boolean existsPermCode(String permCode, Long excludeId) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermCode, permCode)
                .eq(SysPermission::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(SysPermission::getId, excludeId);
        }
        return permissionMapper.selectCount(wrapper) > 0;
    }

    private String normalizeType(String value) {
        String type = normalizeRequired(value, "权限类型不能为空").toUpperCase();
        if (!VALID_TYPES.contains(type)) {
            throw new BusinessException("权限类型仅支持 MENU 或 BUTTON");
        }
        return type;
    }

    private String normalizePermCode(String value) {
        String code = normalizeRequired(value, "权限编码不能为空");
        if (!code.matches("^[a-z0-9:_-]{3,128}$")) {
            throw new BusinessException("权限编码格式非法（仅支持小写字母、数字、:、_、-）");
        }
        return code;
    }

    private String normalizeStatus(String value) {
        String status = StringUtils.hasText(value) ? value.trim().toUpperCase() : "ENABLED";
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException("状态仅支持 ENABLED 或 DISABLED");
        }
        return status;
    }

    private String normalizeRequired(String value, String msg) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(msg);
        }
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
