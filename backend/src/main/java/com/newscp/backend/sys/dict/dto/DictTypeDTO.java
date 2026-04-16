package com.newscp.backend.sys.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DictTypeDTO(
        @NotBlank(message = "类型编码不能为空")
        String typeCode,
        @NotBlank(message = "类型名称不能为空")
        String typeName,
        @NotBlank(message = "来源不能为空")
        String source,
        @NotNull(message = "editable不能为空")
        Integer editable,
        @NotNull(message = "status不能为空")
        Integer status,
        Integer sortOrder,
        String remark
) {
}
