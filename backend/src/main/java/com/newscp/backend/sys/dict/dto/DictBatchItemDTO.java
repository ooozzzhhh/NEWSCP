package com.newscp.backend.sys.dict.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DictBatchItemDTO(
        @NotEmpty(message = "items不能为空")
        List<SortItem> items
) {
    public record SortItem(
            @NotNull(message = "id不能为空")
            Long id,
            @NotNull(message = "sortOrder不能为空")
            Integer sortOrder
    ) {
    }
}
