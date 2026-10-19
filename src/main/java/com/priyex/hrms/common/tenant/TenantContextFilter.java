package com.priyex.hrms.common.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that establishes TenantContext at the beginning of each HTTP request
 * based on headers (X-Company-Id / X-Tenant-Code) and guarantees cleanup at completion.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String COMPANY_ID_HEADER = "X-Company-Id";
    public static final String TENANT_CODE_HEADER = "X-Tenant-Code";

    public TenantContextFilter() {
        super();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String companyIdHeader = request.getHeader(COMPANY_ID_HEADER);
        String tenantCodeHeader = request.getHeader(TENANT_CODE_HEADER);

        if (companyIdHeader != null && !companyIdHeader.isBlank()) {
            try {
                TenantContext.setCompanyId(Long.parseLong(companyIdHeader.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore invalid header format
            }
        }

        if (tenantCodeHeader != null && !tenantCodeHeader.isBlank()) {
            TenantContext.setTenantCode(tenantCodeHeader.trim().toUpperCase());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
