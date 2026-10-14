package com.priyex.hrms.common.email;

public interface EmailService {
    void sendOnboardingInvite(String recipientEmail, String workEmail, String recipientName, String tempPassword, String loginUrl);
    void sendOnboardingApprovalNotification(String recipientEmail, String recipientName);
}
