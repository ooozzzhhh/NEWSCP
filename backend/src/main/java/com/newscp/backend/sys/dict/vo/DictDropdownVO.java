package com.newscp.backend.sys.dict.vo;

public record DictDropdownVO(
        String typeCode,
        String value,
        String label,
        String color,
        Integer isDefault,
        Integer sortOrder
) {
}
