package com.priyex.hrms.organization.service;

import com.priyex.hrms.organization.dto.ClientOverviewDto;
import com.priyex.hrms.organization.dto.DomainDetectionResult;
import com.priyex.hrms.organization.dto.OnboardClientRequest;

import java.util.List;

public interface ClientOnboardingService {

    List<ClientOverviewDto> getAllClients();

    ClientOverviewDto getClientById(Long id);

    ClientOverviewDto onboardClient(OnboardClientRequest request, Long currentUserId);

    void toggleClientStatus(Long id, boolean isActive);

    DomainDetectionResult detectDomain(String emailOrDomain);

    void addDomain(Long clientId, String domainName, boolean isPrimary);

    void removeDomain(Long clientId, Long domainId);

    void updateClientLogo(Long clientId, String logoUrl);
}
