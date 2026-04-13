package com.newscp.backend.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
    private static final String DEFAULT_TENANT_ID = "demo-tenant";

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

    public static void clear() {
        CONTEXT.remove();
    }
}
