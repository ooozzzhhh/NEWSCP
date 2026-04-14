package com.newscp.backend.sys.menu;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.common.security.SecurityUtils;
import com.newscp.backend.sys.menu.vo.MenuResponseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<MenuResponseVO> getCurrentUserMenu() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        return ApiResponse.ok(menuService.getCurrentUserMenu(userId));
    }
}
