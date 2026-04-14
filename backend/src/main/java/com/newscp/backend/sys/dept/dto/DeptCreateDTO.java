package com.newscp.backend.sys.dept.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeptCreateDTO(
        @NotBlank(message = "部门名称不能为空") String deptName,
        @NotNull(message = "上级部门不能为空") Long parentId,
        Long leaderId,
        Integer sortOrder,
        String remark
) {
}
