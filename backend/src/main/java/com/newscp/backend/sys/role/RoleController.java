package com.newscp.backend.sys.role;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.common.PageResult;
import com.newscp.backend.sys.role.dto.RoleAssignPermDTO;
import com.newscp.backend.sys.role.dto.RoleCreateDTO;
import com.newscp.backend.sys.role.dto.RolePageQueryDTO;
import com.newscp.backend.sys.role.dto.RoleUpdateDTO;
import com.newscp.backend.sys.role.vo.PermissionTreeVO;
import com.newscp.backend.sys.role.vo.RoleDetailVO;
import com.newscp.backend.sys.role.vo.RoleOptionVO;
import com.newscp.backend.sys.role.vo.RoleVO;
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
@RequestMapping("/api/sys")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('sys:role:list')")
    @GetMapping("/roles")
    public ApiResponse<PageResult<RoleVO>> page(RolePageQueryDTO query) {
        return ApiResponse.ok(roleService.page(query));
    }

    @PreAuthorize("hasAuthority('sys:role:create')")
    @PostMapping("/roles")
    public ApiResponse<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return ApiResponse.ok(roleService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:role:read')")
    @GetMapping("/roles/{id}")
    public ApiResponse<RoleDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(roleService.detail(id));
    }

    @PreAuthorize("hasAuthority('sys:role:update')")
    @PutMapping("/roles/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        roleService.update(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:role:delete')")
    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/roles/all")
    public ApiResponse<List<RoleOptionVO>> all() {
        return ApiResponse.ok(roleService.listAll());
    }

    @PreAuthorize("hasAuthority('sys:role:assign-perm')")
    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @RequestBody RoleAssignPermDTO dto) {
        roleService.assignPermissions(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:role:assign-perm')")
    @GetMapping("/permissions/tree")
    public ApiResponse<List<PermissionTreeVO>> permissionTree() {
        return ApiResponse.ok(roleService.permissionTree());
    }
}
