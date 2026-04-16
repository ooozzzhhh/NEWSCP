package com.newscp.backend.sys.dict.vo;

public record DictTypeVO(
        Long id,
        String tenantId,
        String typeCode,
        String typeName,
        String source,
        Integer editable,
        Integer status,
        Integer sortOrder,
        String remark
) {
}
