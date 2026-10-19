package com.priyex.hrms.common.tenant;

/**
 * ThreadLocal context holding the active tenant/company scope for the current request.
 * Allows repository queries and service logic to transparently enforce multi-client data isolation.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_COMPANY_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_CODE = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    public static void setCompanyId(Long companyId) {
        CURRENT_COMPANY_ID.set(companyId);
    }

    public static Long getCompanyId() {
        Long id = CURRENT_COMPANY_ID.get();
        return id != null ? id : 1L; // default to 1L if not set
    }

    public static Long getCurrentCompanyId() {
        return CURRENT_COMPANY_ID.get();
    }

    public static void setTenantCode(String code) {
        CURRENT_TENANT_CODE.set(code);
    }

    public static String getTenantCode() {
        return CURRENT_TENANT_CODE.get();
    }

    public static void clear() {
        CURRENT_COMPANY_ID.remove();
        CURRENT_TENANT_CODE.remove();
    }
}
