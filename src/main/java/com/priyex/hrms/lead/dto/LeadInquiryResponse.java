package com.priyex.hrms.lead.dto;

import java.time.LocalDateTime;

public class LeadInquiryResponse {

    private String inquiryId;
    private String status;
    private String message;
    private String assignedConsultant;
    private String scheduledWindow;
    private String confirmationEmail;
    private LocalDateTime submittedAt;

    public LeadInquiryResponse() {
    }

    public LeadInquiryResponse(String inquiryId, String status, String message, String assignedConsultant, String scheduledWindow, String confirmationEmail, LocalDateTime submittedAt) {
        this.inquiryId = inquiryId;
        this.status = status;
        this.message = message;
        this.assignedConsultant = assignedConsultant;
        this.scheduledWindow = scheduledWindow;
        this.confirmationEmail = confirmationEmail;
        this.submittedAt = submittedAt;
    }

    public String getInquiryId() {
        return inquiryId;
    }

    public void setInquiryId(String inquiryId) {
        this.inquiryId = inquiryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAssignedConsultant() {
        return assignedConsultant;
    }

    public void setAssignedConsultant(String assignedConsultant) {
        this.assignedConsultant = assignedConsultant;
    }

    public String getScheduledWindow() {
        return scheduledWindow;
    }

    public void setScheduledWindow(String scheduledWindow) {
        this.scheduledWindow = scheduledWindow;
    }

    public String getConfirmationEmail() {
        return confirmationEmail;
    }

    public void setConfirmationEmail(String confirmationEmail) {
        this.confirmationEmail = confirmationEmail;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
