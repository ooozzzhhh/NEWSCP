package com.newscp.backend.sys.dict;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.dict.dto.DictBatchItemDTO;
import com.newscp.backend.sys.dict.dto.DictItemDTO;
import com.newscp.backend.sys.dict.vo.DictDropdownVO;
import com.newscp.backend.sys.dict.vo.DictItemVO;
import com.newscp.backend.tenant.TenantContext;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys")
public class DictItemController {

    private final DictService dictService;

    public DictItemController(DictService dictService) {
        this.dictService = dictService;
    }

    @PreAuthorize("hasAuthority('sys:dict:list')")
    @GetMapping("/dict-items")
    public ApiResponse<List<DictItemVO>> listItems(@RequestParam String typeCode) {
        return ApiResponse.ok(dictService.listItems(TenantContext.getTenantId(), typeCode));
    }

    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @PostMapping("/dict-items")
    public ApiResponse<Long> createItem(@Valid @RequestBody DictItemDTO dto) {
        return ApiResponse.ok(dictService.createItem(TenantContext.getTenantId(), dto));
    }

    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @PutMapping("/dict-items/{id}")
    public ApiResponse<Void> updateItem(@PathVariable Long id, @Valid @RequestBody DictItemDTO dto) {
        dictService.updateItem(TenantContext.getTenantId(), id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @DeleteMapping("/dict-items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(TenantContext.getTenantId(), id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @PutMapping("/dict-items/batch-sort")
    public ApiResponse<Void> batchSort(@Valid @RequestBody DictBatchItemDTO dto) {
        dictService.batchSortItems(TenantContext.getTenantId(), dto);
        return ApiResponse.ok();
    }

    @GetMapping("/dict/dropdown")
    public ApiResponse<Map<String, List<DictDropdownVO>>> dropdown(@RequestParam String typeCodes) {
        List<String> codes = Arrays.stream(typeCodes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return ApiResponse.ok(dictService.dropdown(TenantContext.getTenantId(), codes));
    }
}
