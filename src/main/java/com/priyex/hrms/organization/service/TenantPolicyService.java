package com.priyex.hrms.organization.service;

import com.priyex.hrms.organization.dto.CompanyDto;
import com.priyex.hrms.organization.dto.TenantPolicyConfigDto;

import java.util.List;

public interface TenantPolicyService {

    TenantPolicyConfigDto getPolicyConfig(Long companyId);

    TenantPolicyConfigDto updatePolicyConfig(Long companyId, TenantPolicyConfigDto updateDto);

    List<CompanyDto> getAllClients();
}
