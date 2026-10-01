package com.priyex.hrms.employee.service;

import com.priyex.hrms.employee.dto.InitiateOnboardingRequest;
import com.priyex.hrms.employee.dto.InitiateOnboardingResponse;
import com.priyex.hrms.employee.dto.PendingOnboardingDto;
import com.priyex.hrms.employee.dto.SubmitOnboardingRequest;
import com.priyex.hrms.employee.model.Employee;

import java.util.List;

public interface OnboardingService {

    InitiateOnboardingResponse initiateOnboarding(Long companyId, Long actorId, InitiateOnboardingRequest req, String baseUrl);

    Employee submitOnboarding(Long companyId, Long userId, Long employeeId, SubmitOnboardingRequest req);

    List<PendingOnboardingDto> getPendingOnboardings(Long companyId);

    Employee approveOnboarding(Long companyId, Long actorId, Long employeeId, String remarks);

    Employee requestRevisions(Long companyId, Long actorId, Long employeeId, String feedbackNotes);
}
