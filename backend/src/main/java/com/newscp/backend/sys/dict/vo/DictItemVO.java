package com.newscp.backend.sys.dict.vo;

public record DictItemVO(
        Long id,
        String tenantId,
        String typeCode,
        String value,
        String label,
        String labelEn,
        String color,
        String extra,
        Integer sortOrder,
        Integer status,
        Integer isDefault,
        String remark
) {
}
