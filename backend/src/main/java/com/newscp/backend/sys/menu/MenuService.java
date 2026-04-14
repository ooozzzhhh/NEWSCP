package com.newscp.backend.sys.menu;

import com.newscp.backend.sys.menu.vo.MenuNodeVO;
import com.newscp.backend.sys.menu.vo.MenuResponseVO;
import com.newscp.backend.sys.role.entity.SysPermission;
import com.newscp.backend.sys.role.mapper.SysPermissionMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final SysPermissionMapper permissionMapper;

    public MenuService(SysPermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public MenuResponseVO getCurrentUserMenu(Long userId) {
        List<SysPermission> allPerms = permissionMapper.selectByUserId(userId);
        List<SysPermission> menuPerms = allPerms.stream()
                .filter(p -> "MENU".equalsIgnoreCase(p.getPermType()))
                .toList();
        List<String> buttonCodes = allPerms.stream()
                .filter(p -> "BUTTON".equalsIgnoreCase(p.getPermType()))
                .map(SysPermission::getPermCode)
                .distinct()
                .sorted()
                .toList();

        return new MenuResponseVO(buildMenuTree(menuPerms), buttonCodes);
    }

    private List<MenuNodeVO> buildMenuTree(List<SysPermission> menuPerms) {
        Map<Long, MenuNodeVO> nodeMap = new HashMap<>();
        for (SysPermission perm : menuPerms) {
            MenuNodeVO node = new MenuNodeVO();
            node.setId(perm.getId());
            node.setPermCode(perm.getPermCode());
            node.setPermName(perm.getPermName());
            node.setRoutePath(perm.getRoutePath());
            node.setComponentPath(perm.getComponentPath());
            node.setIcon(perm.getIcon());
            node.setSortOrder(perm.getSortOrder());
            nodeMap.put(perm.getId(), node);
        }

        List<MenuNodeVO> roots = new ArrayList<>();
        for (SysPermission perm : menuPerms) {
            MenuNodeVO node = nodeMap.get(perm.getId());
            Long parentId = perm.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                roots.add(node);
                continue;
            }
            nodeMap.get(parentId).getChildren().add(node);
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuNodeVO> nodes) {
        nodes.sort(Comparator.comparing(MenuNodeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuNodeVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (MenuNodeVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }
}
