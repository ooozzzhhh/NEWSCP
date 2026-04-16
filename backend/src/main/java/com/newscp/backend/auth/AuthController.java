package com.newscp.backend.auth;

import com.newscp.backend.auth.dto.LoginRequest;
import com.newscp.backend.auth.dto.LoginResponse;
import com.newscp.backend.auth.dto.MeResponse;
import com.newscp.backend.auth.dto.ForceChangePasswordRequest;
import com.newscp.backend.common.ApiResponse;
import com.newscp.backend.common.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/force-change-password")
    public ApiResponse<Void> forceChangePassword(@Valid @RequestBody ForceChangePasswordRequest request) {
        authService.forceChangePassword(request);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me() {
        return ApiResponse.ok(authService.getMe(SecurityUtils.getCurrentUserId()));
    }
}
