package com.newscp.backend.sys.dict.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DictItemDTO(
        @NotBlank(message = "typeCode不能为空")
        String typeCode,
        @NotBlank(message = "value不能为空")
        String value,
        @NotBlank(message = "label不能为空")
        String label,
        String labelEn,
        String color,
        String extra,
        Integer sortOrder,
        @NotNull(message = "status不能为空")
        Integer status,
        @NotNull(message = "isDefault不能为空")
        Integer isDefault,
        String remark
) {
}
