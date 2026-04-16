package com.newscp.backend.sys.security;

import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.tenant.TenantContext;
import com.newscp.backend.sys.security.dto.PasswordPolicyDTO;
import com.newscp.backend.sys.security.vo.PasswordPolicyVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sys/password-policy")
public class PasswordPolicyController {

    private final PasswordPolicyService passwordPolicyService;

    public PasswordPolicyController(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    @PreAuthorize("hasAuthority('sys:security:view')")
    @GetMapping
    public ApiResponse<PasswordPolicyVO> getCurrentPolicy() {
        return ApiResponse.ok(passwordPolicyService.getCurrentPolicy(TenantContext.getTenantId()));
    }

    @PreAuthorize("hasAuthority('sys:security:edit')")
    @PutMapping
    public ApiResponse<Void> updatePolicy(@Valid @RequestBody PasswordPolicyDTO dto) {
        passwordPolicyService.update(TenantContext.getTenantId(), dto);
        return ApiResponse.ok();
    }
}
