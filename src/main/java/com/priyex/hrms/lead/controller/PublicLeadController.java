package com.priyex.hrms.lead.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.lead.dto.LeadInquiryRequest;
import com.priyex.hrms.lead.dto.LeadInquiryResponse;
import com.priyex.hrms.lead.dto.PlatformStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.priyex.hrms.common.email.EmailService;
import com.priyex.hrms.common.whatsapp.service.WhatsAppService;
import com.priyex.hrms.lead.mapper.LeadInquiryMapper;
import com.priyex.hrms.lead.model.LeadInquiry;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public SaaS & Lead Acquisition", description = "Public endpoints for prospective clients, demo requests, and platform capabilities")
public class PublicLeadController {

    private static final Logger log = LoggerFactory.getLogger(PublicLeadController.class);

    private final LeadInquiryMapper leadInquiryMapper;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;

    @PostMapping("/leads")
    @Operation(summary = "Submit a demo request or sales consultation", description = "Captures prospect contact details, persists inquiry, and dispatches automated email/WhatsApp confirmations")
    public ResponseEntity<ApiResponse<LeadInquiryResponse>> submitLead(@Valid @RequestBody LeadInquiryRequest request) {
        log.info("Received public lead inquiry: name={}, company={}, email={}, phone={}, type={}",
                request.getFullName(), request.getCompanyName(), request.getWorkEmail(),
                request.getPhoneNumber(), request.getInquiryType());

        int randomRef = ThreadLocalRandom.current().nextInt(1000, 9999);
        String prefix = "DEMO_REQUEST".equalsIgnoreCase(request.getInquiryType()) ? "PRX-DEMO" : "PRX-SALES";
        String inquiryId = prefix + "-2026-" + randomRef;

        String assignedConsultant = "Priyex Enterprise Solutions Specialist";
        String scheduledWindow = (request.getPreferredDate() != null && !request.getPreferredDate().isBlank())
                ? request.getPreferredDate() + (request.getPreferredTimeSlot() != null ? " (" + request.getPreferredTimeSlot() + ")" : " (Morning Slot)")
                : "Within 24 Business Hours";

        // 1. Persist lead inquiry to PostgreSQL database immediately
        Long leadId = null;
        if (leadInquiryMapper != null) {
            try {
                LeadInquiry lead = LeadInquiry.builder()
                        .inquiryRef(inquiryId)
                        .fullName(request.getFullName())
                        .workEmail(request.getWorkEmail())
                        .companyName(request.getCompanyName())
                        .phoneNumber(request.getPhoneNumber())
                        .employeeCount(request.getEmployeeCount())
                        .interestModule(request.getInterestModule())
                        .inquiryType(request.getInquiryType() != null ? request.getInquiryType() : "DEMO_REQUEST")
                        .preferredDate(request.getPreferredDate())
                        .preferredTimeSlot(request.getPreferredTimeSlot())
                        .message(request.getMessage())
                        .status("NEW")
                        .assignedConsultant(assignedConsultant)
                        .emailStatus("PENDING")
                        .whatsappStatus("PENDING")
                        .build();

                leadInquiryMapper.insert(lead);
                leadId = lead.getId();
                log.info("Persisted lead inquiry [{}] into database successfully with ID [{}].", inquiryId, leadId);
            } catch (Exception e) {
                log.warn("Database persistence for lead inquiry [{}] skipped or failed: {}", inquiryId, e.getMessage());
            }
        }

        // 2. Dispatch Email Confirmation to Prospect
        String emailStatus = "PENDING";
        if (request.getWorkEmail() != null && !request.getWorkEmail().isBlank()) {
            try {
                log.info("Dispatching live demo confirmation email to [{}]...", request.getWorkEmail());
                boolean emailSent = emailService.sendDemoConfirmation(
                        request.getWorkEmail(),
                        request.getFullName(),
                        request.getCompanyName(),
                        inquiryId,
                        scheduledWindow,
                        assignedConsultant
                );
                emailStatus = emailSent ? "SENT" : "FAILED";
                log.info("Demo confirmation email result for [{}]: {}", inquiryId, emailStatus);
            } catch (Exception e) {
                log.error("Demo confirmation email dispatch failed for [{}]: {}", inquiryId, e.getMessage(), e);
                emailStatus = "FAILED";
            }
        }

        // 3. Dispatch Internal Lead Alert Email to leadership & sales (tech.priyex@gmail.com, in.rkumar91@gmail.com)
        try {
            log.info("Dispatching internal lead alert email for [{} - {}]...", request.getCompanyName(), inquiryId);
            boolean alertSent = emailService.sendInternalLeadAlert(
                    request.getCompanyName(),
                    request.getFullName(),
                    request.getWorkEmail(),
                    request.getPhoneNumber(),
                    request.getEmployeeCount(),
                    request.getInterestModule(),
                    scheduledWindow,
                    inquiryId
            );
            log.info("Internal lead alert email result for [{}]: {}", inquiryId, alertSent ? "SENT" : "FAILED");
        } catch (Exception e) {
            log.error("Internal lead alert email dispatch failed for [{}]: {}", inquiryId, e.getMessage(), e);
        }

        // 4. Dispatch WhatsApp Notifications (already @Async in WhatsAppServiceImpl)
        String whatsappStatus = "PENDING";
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            try {
                boolean waSent = whatsAppService.sendDemoBookingConfirmation(
                        request.getPhoneNumber(),
                        request.getFullName(),
                        request.getCompanyName(),
                        inquiryId,
                        scheduledWindow,
                        assignedConsultant
                );
                whatsappStatus = waSent ? "SENT" : "MOCK_SENT";
            } catch (Exception e) {
                log.warn("WhatsApp end-user dispatch error for [{}]: {}", inquiryId, e.getMessage());
                whatsappStatus = "FAILED";
            }
        } else {
            whatsappStatus = "SKIPPED_NO_PHONE";
        }

        try {
            whatsAppService.sendInternalSalesAlert(
                    null,
                    request.getCompanyName(),
                    request.getFullName(),
                    request.getWorkEmail(),
                    request.getPhoneNumber(),
                    request.getEmployeeCount(),
                    request.getInterestModule(),
                    scheduledWindow,
                    inquiryId
            );
        } catch (Exception e) {
            log.warn("Priyex Sales Group WhatsApp alert dispatch error: {}", e.getMessage());
        }

        // 5. Update delivery audit statuses in DB if record was persisted
        if (leadId != null) {
            try {
                leadInquiryMapper.updateDeliveryStatuses(leadId, emailStatus, whatsappStatus);
            } catch (Exception e) {
                log.warn("Failed to update delivery audit statuses for lead [{}]: {}", leadId, e.getMessage());
            }
        }

        String message = "Thank you, " + request.getFullName() + "! Your consultation request for "
                + request.getCompanyName() + " has been received. Our enterprise team will connect with you shortly.";

        LeadInquiryResponse response = new LeadInquiryResponse(
                inquiryId,
                "CONFIRMED",
                message,
                assignedConsultant,
                scheduledWindow,
                request.getWorkEmail(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Demo consultation request submitted successfully."));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform capabilities for marketing landing page", description = "Returns uptime SLA, supported modules and compliance standards")
    public ResponseEntity<ApiResponse<PlatformStatsResponse>> getPlatformStats() {
        PlatformStatsResponse stats = new PlatformStatsResponse(
                100, // Generic architectural scalability metric
                50000,
                "99.99%",
                "99.98%",
                "Under 45 minutes",
                List.of(
                        "Core HR & Employee Lifecycle Management",
                        "Automated Multi-State Payroll & Statutory Tax",
                        "Real-Time Biometric & Geofenced Time Attendance",
                        "Leave Quotas & Multi-Tier Approval Workflows",
                        "Expense Claims & Reimbursement Management",
                        "Multi-Tenant Cloud Policy Engine",
                        "Employee Help Desk & In-App Broadcast Alerts"
                ),
                List.of(
                        "SOC 2 Type II Ready",
                        "ISO/IEC 27001 Security Standard",
                        "Digital Personal Data Protection (DPDP) Standard",
                        "Statutory EPF, ESI, PT & Income Tax Regulations"
                )
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
