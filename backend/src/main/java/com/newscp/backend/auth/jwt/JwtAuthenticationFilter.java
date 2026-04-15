package com.newscp.backend.auth.jwt;

import com.newscp.backend.tenant.TenantContext;
import com.newscp.backend.tenant.mapper.SysTenantMapper;
import com.newscp.backend.tenant.mapper.SysUserTenantMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final SysTenantMapper tenantMapper;
    private final SysUserTenantMapper userTenantMapper;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService,
            SysTenantMapper tenantMapper,
            SysUserTenantMapper userTenantMapper
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.tenantMapper = tenantMapper;
        this.userTenantMapper = userTenantMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String userId = claims.getSubject();
                String tenantId = claims.get("tenantId", String.class);
                List<String> roleCodes = claims.get("roles", List.class);
                boolean isSuperAdmin = roleCodes != null && roleCodes.contains(TenantContext.SUPER_ADMIN_ROLE);
                String tenantFromHeader = request.getHeader(TENANT_HEADER);
                String runtimeTenantId = tenantId;
                if (tenantFromHeader != null && !tenantFromHeader.isBlank()) {
                    runtimeTenantId = resolveRuntimeTenantId(
                            Long.parseLong(userId),
                            tenantId,
                            tenantFromHeader.trim(),
                            isSuperAdmin
                    );
                }
                TenantContext.setTenantId(runtimeTenantId);
                TenantContext.setRoleCodes(roleCodes);
                TenantContext.setSuperAdmin(isSuperAdmin);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveRuntimeTenantId(Long userId, String tokenTenantId, String requestTenantId, boolean isSuperAdmin) {
        if (requestTenantId.equals(tokenTenantId)) {
            return tokenTenantId;
        }
        if (isSuperAdmin) {
            List<String> enabled = tenantMapper.selectEnabledTenantIds(List.of(requestTenantId));
            if (!enabled.isEmpty()) {
                return requestTenantId;
            }
            return tokenTenantId;
        }
        List<String> bound = userTenantMapper.selectBoundTenantIds(userId, List.of(requestTenantId));
        if (!bound.isEmpty()) {
            List<String> enabled = tenantMapper.selectEnabledTenantIds(bound);
            if (!enabled.isEmpty()) {
                return requestTenantId;
            }
        }
        return tokenTenantId;
    }
}
