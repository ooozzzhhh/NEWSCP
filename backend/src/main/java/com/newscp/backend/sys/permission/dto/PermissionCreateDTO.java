package com.newscp.backend.sys.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermissionCreateDTO(
        @NotNull(message = "父节点不能为空") Long parentId,
        @NotBlank(message = "权限类型不能为空") String permType,
        @NotBlank(message = "权限编码不能为空") String permCode,
        @NotBlank(message = "权限名称不能为空") String permName,
        String routePath,
        String componentPath,
        String icon,
        Integer sortOrder,
        Integer isHidden,
        String status
) {
}
