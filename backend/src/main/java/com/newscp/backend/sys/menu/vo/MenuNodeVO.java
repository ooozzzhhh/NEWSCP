package com.newscp.backend.sys.menu.vo;

import java.util.ArrayList;
import java.util.List;

public class MenuNodeVO {

    private Long id;
    private String permCode;
    private String permName;
    private String routePath;
    private String componentPath;
    private String icon;
    private Integer sortOrder;
    private List<MenuNodeVO> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<MenuNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<MenuNodeVO> children) {
        this.children = children;
    }
}
