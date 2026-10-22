package com.priyex.hrms.common.email;

public interface EmailService {
    boolean sendOnboardingInvite(String recipientEmail, String workEmail, String recipientName, String tempPassword, String loginUrl);
    boolean sendOnboardingApprovalNotification(String recipientEmail, String recipientName);
    boolean sendDemoConfirmation(String recipientEmail, String recipientName, String companyName, String inquiryRef, String scheduledWindow, String assignedConsultant);
}
