package com.newscp.backend.tenant;

import java.util.List;

public final class TenantContext {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLE_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SUPER_ADMIN_CONTEXT = new ThreadLocal<>();
    private static final String DEFAULT_TENANT_ID = "admin";
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN_ROLE";

    private TenantContext() {
    }

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return;
        }
        CONTEXT.set(tenantId);
    }

    public static String getCurrentTenantId() {
        return CONTEXT.get();
    }

    public static String getTenantId() {
        String tenantId = CONTEXT.get();
        if (tenantId == null || tenantId.isBlank()) {
            return DEFAULT_TENANT_ID;
        }
        return tenantId;
    }

    public static void setRoleCodes(List<String> roleCodes) {
        ROLE_CONTEXT.set(roleCodes == null ? List.of() : roleCodes);
    }

    public static List<String> getRoleCodes() {
        List<String> roleCodes = ROLE_CONTEXT.get();
        return roleCodes == null ? List.of() : roleCodes;
    }

    public static void setSuperAdmin(boolean superAdmin) {
        SUPER_ADMIN_CONTEXT.set(superAdmin);
    }

    public static boolean isSuperAdmin() {
        Boolean value = SUPER_ADMIN_CONTEXT.get();
        return Boolean.TRUE.equals(value);
    }

    public static void clear() {
        CONTEXT.remove();
        ROLE_CONTEXT.remove();
        SUPER_ADMIN_CONTEXT.remove();
    }
}
