package com.newscp.backend.sys.dict;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.dict.dto.DictTypeDTO;
import com.newscp.backend.sys.dict.vo.DictTypeVO;
import com.newscp.backend.tenant.TenantContext;
import jakarta.validation.Valid;
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
@RequestMapping("/api/sys/dict-types")
public class DictTypeController {

    private final DictService dictService;

    public DictTypeController(DictService dictService) {
        this.dictService = dictService;
    }

    @PreAuthorize("hasAuthority('sys:dict:list')")
    @GetMapping
    public ApiResponse<PageResult<DictTypeVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(dictService.pageTypes(TenantContext.getTenantId(), page, size, keyword));
    }

    @PreAuthorize("hasAuthority('sys:dict:list')")
    @GetMapping("/{id}")
    public ApiResponse<DictTypeVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(dictService.getType(TenantContext.getTenantId(), id));
    }

    @PreAuthorize("hasAuthority('sys:dict:create')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody DictTypeDTO dto) {
        return ApiResponse.ok(dictService.createType(TenantContext.getTenantId(), dto));
    }

    @PreAuthorize("hasAuthority('sys:dict:edit')")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DictTypeDTO dto) {
        dictService.updateType(TenantContext.getTenantId(), id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:dict:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictService.deleteType(TenantContext.getTenantId(), id);
        return ApiResponse.ok();
    }
}
