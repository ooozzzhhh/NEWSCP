package com.newscp.backend.sys.role.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RoleCreateDTO(
        @NotBlank(message = "角色编码不能为空") String roleCode,
        @NotBlank(message = "角色名称不能为空") String roleName,
        String remark,
        Integer sortOrder,
        List<Long> permIds
) {
}
