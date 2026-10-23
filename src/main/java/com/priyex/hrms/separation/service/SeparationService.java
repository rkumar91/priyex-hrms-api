package com.priyex.hrms.separation.service;

import com.priyex.hrms.separation.dto.*;
import com.priyex.hrms.separation.model.ExitClearance;
import com.priyex.hrms.separation.model.SeparationCase;
import com.priyex.hrms.separation.model.SeparationPolicy;

import java.util.List;

public interface SeparationService {

    SeparationPolicy getPolicy(Long companyId);

    SeparationPolicy updatePolicy(Long companyId, SeparationPolicy policy);

    SeparationCase submitResignation(Long companyId, Long employeeId, Long userId, SubmitResignationRequest request);

    SeparationCase withdrawResignation(Long companyId, Long separationId, Long employeeId);

    SeparationCase initiateTermination(Long companyId, Long adminUserId, InitiateTerminationRequest request);

    SeparationCase reviewSeparation(Long companyId, Long separationId, Long reviewerUserId, boolean isHr, ReviewSeparationRequest request);

    ExitClearance updateClearance(Long companyId, Long clearanceId, Long reviewerUserId, UpdateClearanceRequest request);

    SeparationCase finalizeExit(Long companyId, Long separationId, Long adminUserId, FinalizeExitRequest request);

    List<SeparationCase> getSeparationCases(Long companyId, String status, Long employeeId);

    SeparationCase getSeparationById(Long companyId, Long separationId);

    SeparationCase getMySeparation(Long companyId, Long employeeId);
}
