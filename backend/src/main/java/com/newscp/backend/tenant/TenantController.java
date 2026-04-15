package com.newscp.backend.tenant;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.tenant.dto.TenantCreateDTO;
import com.newscp.backend.tenant.dto.TenantPageQueryDTO;
import com.newscp.backend.tenant.dto.TenantUpdateDTO;
import com.newscp.backend.tenant.dto.TenantUserAssignDTO;
import com.newscp.backend.tenant.vo.TenantUserVO;
import com.newscp.backend.tenant.vo.TenantVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PreAuthorize("hasAuthority('sys:tenant:list')")
    @GetMapping
    public ApiResponse<PageResult<TenantVO>> page(TenantPageQueryDTO query) {
        return ApiResponse.ok(tenantService.page(query));
    }

    @PreAuthorize("hasAuthority('sys:tenant:create')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody TenantCreateDTO dto) {
        return ApiResponse.ok(tenantService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:tenant:read')")
    @GetMapping("/{id}")
    public ApiResponse<TenantVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(tenantService.detail(id));
    }

    @PreAuthorize("hasAuthority('sys:tenant:update')")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TenantUpdateDTO dto) {
        tenantService.update(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:tenant:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:tenant:read')")
    @GetMapping("/{id}/users")
    public ApiResponse<List<TenantUserVO>> users(@PathVariable Long id) {
        return ApiResponse.ok(tenantService.listUsers(id));
    }

    @PreAuthorize("hasAuthority('sys:tenant:assign-user')")
    @PutMapping("/{id}/users")
    public ApiResponse<Void> assignUsers(@PathVariable Long id, @Valid @RequestBody TenantUserAssignDTO dto) {
        tenantService.assignUsers(id, dto);
        return ApiResponse.ok();
    }
}
