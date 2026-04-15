package com.newscp.backend.sys.permission;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.permission.dto.PermissionCreateDTO;
import com.newscp.backend.sys.permission.dto.PermissionUpdateDTO;
import com.newscp.backend.sys.role.vo.PermissionTreeVO;
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
@RequestMapping("/api/sys/permissions")
public class PermissionOperateController {

    private final PermissionOperateService permissionService;

    public PermissionOperateController(PermissionOperateService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAuthority('sys:perm:list')")
    @GetMapping("/operate-tree")
    public ApiResponse<List<PermissionTreeVO>> tree() {
        return ApiResponse.ok(permissionService.tree());
    }

    @PreAuthorize("hasAuthority('sys:perm:create')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody PermissionCreateDTO dto) {
        return ApiResponse.ok(permissionService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:perm:update')")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PermissionUpdateDTO dto) {
        permissionService.update(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:perm:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.ok();
    }
}
