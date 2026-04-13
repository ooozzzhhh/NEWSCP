package com.newscp.backend.auth;

import com.newscp.backend.auth.dto.LoginRequest;
import com.newscp.backend.auth.dto.LoginResponse;
import com.newscp.backend.auth.jwt.JwtTokenProvider;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String DEFAULT_TENANT_ID = "demo-tenant";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        if (!"admin".equals(request.username()) || !"123456".equals(request.password())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        String userId = "u-admin";
        List<String> roles = List.of("ADMIN");
        String tenantId = request.tenantId() == null || request.tenantId().isBlank()
                ? DEFAULT_TENANT_ID
                : request.tenantId().trim();

        String token = jwtTokenProvider.generateToken(userId, request.username(), tenantId, roles);
        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationSeconds(),
                userId,
                request.username(),
                tenantId,
                roles
        );
    }
}
