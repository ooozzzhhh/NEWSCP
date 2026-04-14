package com.newscp.backend.sys.dept;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.sys.dept.dto.DeptCreateDTO;
import com.newscp.backend.sys.dept.dto.DeptUpdateDTO;
import com.newscp.backend.sys.dept.vo.DeptTreeVO;
import com.newscp.backend.sys.dept.vo.DeptUserVO;
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
@RequestMapping("/api/sys/depts")
public class DeptController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @PreAuthorize("hasAuthority('sys:dept:list')")
    @GetMapping("/tree")
    public ApiResponse<List<DeptTreeVO>> tree() {
        return ApiResponse.ok(deptService.tree());
    }

    @PreAuthorize("hasAuthority('sys:dept:list')")
    @GetMapping
    public ApiResponse<List<DeptTreeVO>> list() {
        return ApiResponse.ok(deptService.listFlat());
    }

    @PreAuthorize("hasAuthority('sys:dept:create')")
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody DeptCreateDTO dto) {
        return ApiResponse.ok(deptService.create(dto));
    }

    @PreAuthorize("hasAuthority('sys:dept:update')")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateDTO dto) {
        deptService.update(id, dto);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:dept:delete')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ApiResponse.ok();
    }

    @PreAuthorize("hasAuthority('sys:dept:list')")
    @GetMapping("/{id}/users")
    public ApiResponse<List<DeptUserVO>> users(@PathVariable Long id) {
        return ApiResponse.ok(deptService.users(id));
    }
}
