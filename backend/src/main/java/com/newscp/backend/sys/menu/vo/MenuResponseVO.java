package com.newscp.backend.sys.menu.vo;

import java.util.List;

public record MenuResponseVO(
        List<MenuNodeVO> menus,
        List<String> permissions
) {
}
