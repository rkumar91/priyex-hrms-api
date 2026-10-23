package com.priyex.hrms.separation.service.impl;

import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.separation.dto.*;
import com.priyex.hrms.separation.mapper.SeparationMapper;
import com.priyex.hrms.separation.model.ExitClearance;
import com.priyex.hrms.separation.model.SeparationCase;
import com.priyex.hrms.separation.model.SeparationPolicy;
import com.priyex.hrms.separation.service.SeparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeparationServiceImpl implements SeparationService {

    private final SeparationMapper separationMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    public SeparationPolicy getPolicy(Long companyId) {
        SeparationPolicy policy = separationMapper.findPolicyByCompanyId(companyId);
        if (policy == null) {
            policy = SeparationPolicy.builder()
                    .companyId(companyId)
                    .defaultNoticeDays(60)
                    .probationNoticeDays(30)
                    .allowResignationWithdrawal(true)
                    .autoDeactivateUser(true)
                    .requireManagerApproval(true)
                    .requireItClearance(true)
                    .requireFinanceClearance(true)
                    .build();
            separationMapper.insertPolicy(policy);
        }
        return policy;
    }

    @Override
    @Transactional
    public SeparationPolicy updatePolicy(Long companyId, SeparationPolicy req) {
        SeparationPolicy existing = separationMapper.findPolicyByCompanyId(companyId);
        if (existing == null) {
            req.setCompanyId(companyId);
            separationMapper.insertPolicy(req);
            return req;
        }
        req.setCompanyId(companyId);
        separationMapper.updatePolicy(req);
        return separationMapper.findPolicyByCompanyId(companyId);
    }

    @Override
    @Transactional
    public SeparationCase submitResignation(Long companyId, Long employeeId, Long userId, SubmitResignationRequest req) {
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee #" + employeeId + " not found"));
        if ("EXITED".equalsIgnoreCase(employee.getStatus())) {
            throw new BadRequestException("Employee has already exited the organization");
        }

        SeparationCase existingActive = separationMapper.findActiveSeparationByEmployeeId(employeeId, companyId);
        if (existingActive != null) {
            throw new BadRequestException("An active separation case (#" + existingActive.getId() + ") is already in progress with status: " + existingActive.getStatus());
        }

        SeparationPolicy policy = getPolicy(companyId);
        boolean isProbation = "PROBATION".equalsIgnoreCase(employee.getStatus());
        int noticeDays = isProbation ? policy.getProbationNoticeDays() : (employee.getNoticePeriodDays() != null && employee.getNoticePeriodDays() > 0 ? employee.getNoticePeriodDays() : policy.getDefaultNoticeDays());

        LocalDate submissionDate = LocalDate.now();
        LocalDate standardLwd = submissionDate.plusDays(noticeDays);
        LocalDate requestedLwd = req.getRequestedLastWorkingDay();

        int shortfall = 0;
        if (requestedLwd.isBefore(standardLwd)) {
            shortfall = (int) ChronoUnit.DAYS.between(requestedLwd, standardLwd);
        }

        SeparationCase separationCase = SeparationCase.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .caseType("RESIGNATION")
                .initiatorRole("EMPLOYEE")
                .initiatorUserId(userId)
                .reasonCategory(req.getReasonCategory())
                .reasonDetails(req.getReasonDetails())
                .submissionDate(submissionDate)
                .requestedLastWorkingDay(requestedLwd)
                .approvedLastWorkingDay(requestedLwd)
                .noticePeriodDays(noticeDays)
                .isNoticeWaived(false)
                .noticeShortfallDays(shortfall)
                .status("NOTICE_PERIOD")
                .handoverNotes(req.getHandoverNotes())
                .managerApprovalStatus("PENDING")
                .hrApprovalStatus("PENDING")
                .exitInterviewCompleted(false)
                .build();

        separationMapper.insertSeparationCase(separationCase);

        // Update employee status to NOTICE
        separationMapper.updateEmployeeExitStatus(employeeId, companyId, "NOTICE", requestedLwd, req.getReasonCategory());

        // Create Default Clearances
        createDefaultClearances(companyId, separationCase.getId());

        log.info("Resignation case #{} created for employee #{} ({})", separationCase.getId(), employeeId, employee.getWorkEmail());
        return getSeparationById(companyId, separationCase.getId());
    }

    @Override
    @Transactional
    public SeparationCase withdrawResignation(Long companyId, Long separationId, Long employeeId) {
        SeparationCase separation = separationMapper.findSeparationById(separationId, companyId);
        if (separation == null) {
            throw new ResourceNotFoundException("Separation case #" + separationId + " not found");
        }
        if (!separation.getEmployeeId().equals(employeeId)) {
            throw new BadRequestException("Unauthorized: You can only withdraw your own resignation");
        }
        if ("EXITED".equalsIgnoreCase(separation.getStatus())) {
            throw new BadRequestException("Cannot withdraw: Separation is already completed");
        }

        SeparationPolicy policy = getPolicy(companyId);
        if (!Boolean.TRUE.equals(policy.getAllowResignationWithdrawal())) {
            throw new BadRequestException("Policy restriction: Resignation withdrawal is not enabled for your organization");
        }

        separation.setStatus("WITHDRAWN");
        separationMapper.updateSeparationCase(separation);

        // Restore employee status to ACTIVE
        separationMapper.updateEmployeeExitStatus(employeeId, companyId, "ACTIVE", null, null);

        log.info("Resignation case #{} withdrawn by employee #{}", separationId, employeeId);
        return getSeparationById(companyId, separationId);
    }

    @Override
    @Transactional
    public SeparationCase initiateTermination(Long companyId, Long adminUserId, InitiateTerminationRequest req) {
        Employee employee = employeeMapper.findById(req.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee #" + req.getEmployeeId() + " not found"));
        if ("EXITED".equalsIgnoreCase(employee.getStatus())) {
            throw new BadRequestException("Employee has already exited the organization");
        }

        SeparationCase existingActive = separationMapper.findActiveSeparationByEmployeeId(req.getEmployeeId(), companyId);
        if (existingActive != null) {
            throw new BadRequestException("An active separation case (#" + existingActive.getId() + ") is already in progress");
        }

        LocalDate submissionDate = LocalDate.now();
        LocalDate lwd = req.getLastWorkingDay();

        SeparationCase separationCase = SeparationCase.builder()
                .companyId(companyId)
                .employeeId(req.getEmployeeId())
                .caseType(req.getCaseType() != null ? req.getCaseType().toUpperCase() : "TERMINATION")
                .initiatorRole("HR_ADMIN")
                .initiatorUserId(adminUserId)
                .reasonCategory(req.getReasonCategory())
                .reasonDetails(req.getReasonDetails() + (req.getSeveranceNotes() != null ? "\nSeverance Notes: " + req.getSeveranceNotes() : ""))
                .submissionDate(submissionDate)
                .requestedLastWorkingDay(lwd)
                .approvedLastWorkingDay(lwd)
                .noticePeriodDays((int) ChronoUnit.DAYS.between(submissionDate, lwd))
                .isNoticeWaived(Boolean.TRUE.equals(req.getIsNoticeWaived()))
                .noticeShortfallDays(0)
                .status("NOTICE_PERIOD")
                .handoverNotes(req.getHandoverNotes())
                .managerApprovalStatus("APPROVED")
                .managerReviewedAt(LocalDateTime.now())
                .hrApprovalStatus("APPROVED")
                .hrReviewedAt(LocalDateTime.now())
                .hrReviewedBy(adminUserId)
                .exitInterviewCompleted(false)
                .build();

        separationMapper.insertSeparationCase(separationCase);

        // Update employee status to NOTICE
        separationMapper.updateEmployeeExitStatus(req.getEmployeeId(), companyId, "NOTICE", lwd, req.getReasonCategory());

        // Create Default Clearances
        createDefaultClearances(companyId, separationCase.getId());

        log.info("Termination/Separation initiated for employee #{} by admin #{}", req.getEmployeeId(), adminUserId);
        return getSeparationById(companyId, separationCase.getId());
    }

    @Override
    @Transactional
    public SeparationCase reviewSeparation(Long companyId, Long separationId, Long reviewerUserId, boolean isHr, ReviewSeparationRequest req) {
        SeparationCase separation = separationMapper.findSeparationById(separationId, companyId);
        if (separation == null) {
            throw new ResourceNotFoundException("Separation case #" + separationId + " not found");
        }

        boolean isApprove = "APPROVE".equalsIgnoreCase(req.getAction());

        if (isHr) {
            separation.setHrApprovalStatus(isApprove ? "APPROVED" : "REJECTED");
            separation.setHrRemarks(req.getRemarks());
            separation.setHrReviewedAt(LocalDateTime.now());
            separation.setHrReviewedBy(reviewerUserId);
        } else {
            separation.setManagerApprovalStatus(isApprove ? "APPROVED" : "REJECTED");
            separation.setManagerRemarks(req.getRemarks());
            separation.setManagerReviewedAt(LocalDateTime.now());
            separation.setManagerReviewedBy(reviewerUserId);
        }

        if (req.getApprovedLastWorkingDay() != null) {
            separation.setApprovedLastWorkingDay(req.getApprovedLastWorkingDay());
        }
        if (req.getIsNoticeWaived() != null) {
            separation.setIsNoticeWaived(req.getIsNoticeWaived());
        }

        if (!isApprove) {
            separation.setStatus("REJECTED");
            // Restore employee status to ACTIVE
            separationMapper.updateEmployeeExitStatus(separation.getEmployeeId(), companyId, "ACTIVE", null, null);
        } else {
            separation.setStatus("NOTICE_PERIOD");
            separationMapper.updateEmployeeExitStatus(separation.getEmployeeId(), companyId, "NOTICE", separation.getApprovedLastWorkingDay(), separation.getReasonCategory());
        }

        separationMapper.updateSeparationCase(separation);
        log.info("Separation case #{} reviewed by user #{}: result={}", separationId, reviewerUserId, separation.getStatus());
        return getSeparationById(companyId, separationId);
    }

    @Override
    @Transactional
    public ExitClearance updateClearance(Long companyId, Long clearanceId, Long reviewerUserId, UpdateClearanceRequest req) {
        ExitClearance clearance = separationMapper.findClearanceById(clearanceId, companyId);
        if (clearance == null) {
            throw new ResourceNotFoundException("Exit clearance item #" + clearanceId + " not found");
        }

        clearance.setStatus(req.getStatus().toUpperCase());
        clearance.setClearedByUserId(reviewerUserId);
        clearance.setClearedAt(LocalDateTime.now());
        if (req.getDeductionAmount() != null) {
            clearance.setDeductionAmount(req.getDeductionAmount());
        }
        if (req.getRemarks() != null) {
            clearance.setRemarks(req.getRemarks());
        }

        separationMapper.updateExitClearance(clearance);
        log.info("Clearance item #{} updated to status {}", clearanceId, clearance.getStatus());
        return clearance;
    }

    @Override
    @Transactional
    public SeparationCase finalizeExit(Long companyId, Long separationId, Long adminUserId, FinalizeExitRequest req) {
        SeparationCase separation = separationMapper.findSeparationById(separationId, companyId);
        if (separation == null) {
            throw new ResourceNotFoundException("Separation case #" + separationId + " not found");
        }

        LocalDate exitDate = separation.getApprovedLastWorkingDay() != null ? separation.getApprovedLastWorkingDay() : LocalDate.now();

        separation.setStatus("EXITED");
        separation.setFinalizedBy(adminUserId);
        separation.setFinalizedAt(LocalDateTime.now());
        if (req.getExitInterviewNotes() != null && !req.getExitInterviewNotes().isBlank()) {
            separation.setExitInterviewCompleted(true);
            separation.setExitInterviewNotes(req.getExitInterviewNotes());
        }

        SeparationPolicy policy = getPolicy(companyId);
        boolean deactivate = req.getDeactivateUser() != null ? req.getDeactivateUser() : Boolean.TRUE.equals(policy.getAutoDeactivateUser());

        if (deactivate) {
            separationMapper.deactivateUserByEmployeeId(separation.getEmployeeId(), companyId);
            separation.setUserDeactivatedAt(LocalDateTime.now());
            log.info("User system access automatically DEACTIVATED for employee #{}", separation.getEmployeeId());
        }

        separationMapper.updateSeparationCase(separation);

        // Mark employee EXITED in master directory
        separationMapper.updateEmployeeExitStatus(
                separation.getEmployeeId(),
                companyId,
                "EXITED",
                exitDate,
                separation.getReasonCategory()
        );

        log.info("Employee #{} officially EXITED from organization via case #{}", separation.getEmployeeId(), separationId);
        return getSeparationById(companyId, separationId);
    }

    @Override
    public List<SeparationCase> getSeparationCases(Long companyId, String status, Long employeeId) {
        List<SeparationCase> cases = separationMapper.findSeparationCases(companyId, status, employeeId);
        for (SeparationCase sc : cases) {
            sc.setClearances(separationMapper.findClearancesByCaseId(sc.getId()));
        }
        return cases;
    }

    @Override
    public SeparationCase getSeparationById(Long companyId, Long separationId) {
        SeparationCase sc = separationMapper.findSeparationById(separationId, companyId);
        if (sc != null) {
            sc.setClearances(separationMapper.findClearancesByCaseId(sc.getId()));
        }
        return sc;
    }

    @Override
    public SeparationCase getMySeparation(Long companyId, Long employeeId) {
        SeparationCase sc = separationMapper.findActiveSeparationByEmployeeId(employeeId, companyId);
        if (sc != null) {
            sc.setClearances(separationMapper.findClearancesByCaseId(sc.getId()));
        }
        return sc;
    }

    private void createDefaultClearances(Long companyId, Long separationCaseId) {
        String[][] defaults = new String[][]{
                {"REPORTING_MANAGER", "Knowledge Transfer & Handover", "Complete project handover, documentation, and handover meeting sign-off"},
                {"IT_INFRA", "IT Assets & System Access", "Return laptop/hardware and revoke corporate email, Slack, and cloud credentials"},
                {"ASSETS_ADMIN", "Facilities & Physical Security", "Return company ID card, access keycards, locker keys, and vehicle passes"},
                {"FINANCE_ACCOUNTS", "Financial Dues & Advances", "Settle pending expense claims, corporate credit card balances, and travel advances"},
                {"HR_OPS", "Exit Formalities & Statutory", "Conduct exit interview questionnaire, verify leave encashment, and process F&F clearance"}
        };

        for (String[] def : defaults) {
            ExitClearance clearance = ExitClearance.builder()
                    .separationCaseId(separationCaseId)
                    .companyId(companyId)
                    .departmentType(def[0])
                    .title(def[1])
                    .description(def[2])
                    .status("PENDING")
                    .deductionAmount(BigDecimal.ZERO)
                    .build();
            separationMapper.insertExitClearance(clearance);
        }
    }
}
