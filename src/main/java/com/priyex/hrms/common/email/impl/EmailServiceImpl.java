package com.priyex.hrms.common.email.impl;

import com.priyex.hrms.common.email.EmailDeliveryLog;
import com.priyex.hrms.common.email.EmailLogMapper;
import com.priyex.hrms.common.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailLogMapper emailLogMapper;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.from:tech.priyex@gmail.com}")
    private String fromEmail;

    @Value("${app.mail.alert-recipients:tech.priyex@gmail.com,in.rkumar91@gmail.com}")
    private String alertRecipients;

    @Value("${app.brevo.api-key:}")
    private String brevoApiKey;

    @Value("${app.brevo.sender-name:Priyex People HR Operations}")
    private String brevoSenderName;

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from-email:Priyex HRMS <onboarding@resend.dev>}")
    private String resendFromEmail;

    private boolean dispatchEmail(String recipientEmail, String subject, String body, String templateCode) {
        log.info("\n══════════════════ [EMAIL DISPATCH via Brevo/Resend/SMTP] ══════════════════\nTo: {}\nSubject: {}\n\n{}\n══════════════════════════════════════════════════════════════",
                recipientEmail, subject, body);

        boolean sentReal = false;
        String errorMessage = null;

        // 1. Try Brevo HTTPS REST API (Port 443 — sends reliably from tech.priyex@gmail.com on Render)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            RestClient brevoClient = RestClient.builder().baseUrl("https://api.brevo.com/v3").build();
            List<String> recipients = Arrays.stream(recipientEmail.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String singleRecipient : recipients) {
                try {
                    Map<String, Object> payload = Map.of(
                            "sender", Map.of("name", brevoSenderName, "email", fromEmail),
                            "to", List.of(Map.of("email", singleRecipient)),
                            "subject", subject,
                            "textContent", body
                    );

                    Map response = brevoClient.post()
                            .uri("/smtp/email")
                            .header("api-key", brevoApiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .body(Map.class);

                    log.info("Live email dispatched via Brevo HTTPS API to {}: messageId={}", singleRecipient, response != null ? response.get("messageId") : "OK");
                    sentReal = true;
                    errorMessage = null;
                } catch (Exception ex) {
                    log.warn("Brevo dispatch to {} failed: {}", singleRecipient, ex.getMessage());
                    errorMessage = "Brevo (" + singleRecipient + "): " + ex.getMessage();
                }
            }
        }

        // 2. Fallback to Resend HTTPS REST API (Port 443) if Brevo not configured or failed
        if (!sentReal && resendApiKey != null && !resendApiKey.isBlank()) {
            RestClient restClient = RestClient.builder().baseUrl("https://api.resend.com").build();
            List<String> recipients = Arrays.stream(recipientEmail.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String singleRecipient : recipients) {
                try {
                    Map<String, Object> payload = Map.of(
                            "from", resendFromEmail,
                            "to", List.of(singleRecipient),
                            "subject", subject,
                            "text", body
                    );

                    Map response = restClient.post()
                            .uri("/emails")
                            .header("Authorization", "Bearer " + resendApiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .body(Map.class);

                    log.info("Live email dispatched via Resend HTTPS API to {}: id={}", singleRecipient, response != null ? response.get("id") : "OK");
                    sentReal = true;
                } catch (Exception ex) {
                    log.warn("Resend dispatch to {} failed: {}", singleRecipient, ex.getMessage());
                    errorMessage = "Resend (" + singleRecipient + "): " + ex.getMessage();
                }
            }
        }

        // 2. Fallback to JavaMailSender (SMTP) if Resend not configured or failed
        if (!sentReal) {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender != null) {
                try {
                    String[] recipients = recipientEmail.split(",");
                    for (String r : recipients) {
                        String clean = r.trim();
                        if (!clean.isEmpty()) {
                            SimpleMailMessage msg = new SimpleMailMessage();
                            msg.setFrom(fromEmail);
                            msg.setTo(clean);
                            msg.setSubject(subject);
                            msg.setText(body);
                            mailSender.send(msg);
                        }
                    }
                    sentReal = true;
                    log.info("Live email dispatched via SMTP to {} from {}", recipientEmail, fromEmail);
                    errorMessage = null;
                } catch (Exception e) {
                    log.warn("SMTP dispatch failed: {}", e.getMessage());
                    if (errorMessage == null) errorMessage = "SMTP: " + e.getMessage();
                }
            }
        }

        // 3. Record delivery log
        try {
            EmailDeliveryLog deliveryLog = EmailDeliveryLog.builder()
                    .recipientEmail(recipientEmail != null && recipientEmail.length() > 200 ? recipientEmail.substring(0, 197) + "..." : recipientEmail)
                    .subject(subject != null && subject.length() > 300 ? subject.substring(0, 297) + "..." : subject)
                    .templateCode(templateCode)
                    .status(sentReal ? "SENT" : (errorMessage != null ? "FAILED" : "SENT_OFFLINE"))
                    .errorMessage(errorMessage != null && errorMessage.length() > 500 ? errorMessage.substring(0, 497) + "..." : errorMessage)
                    .build();
            emailLogMapper.insert(deliveryLog);
        } catch (Exception e) {
            log.warn("Failed to write email delivery log: {}", e.getMessage());
        }

        return sentReal;
    }

    @Override
    public boolean sendOnboardingInvite(String recipientEmail, String workEmail, String recipientName, String tempPassword, String loginUrl) {
        String subject = "Welcome to Priyex People — Your Onboarding Credentials";
        String body = String.format("""
            Hello %s,

            Welcome to Priyex People Enterprise HRMS!

            Your onboarding profile has been initiated. Please use your official corporate credentials below to log into the HRMS portal:

            Portal URL: %s
            Login ID (Work Email): %s
            Temporary Password: %s

            *Important*: Upon first login, you will be required to change your temporary password to a permanent secure password and complete your onboarding profile submission.

            Warm regards,
            Priyex People HR Operations
            From: %s
            """, recipientName, loginUrl, workEmail, tempPassword, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, "ONBOARDING_INVITE");
    }

    @Override
    public boolean sendOnboardingApprovalNotification(String recipientEmail, String recipientName) {
        String subject = "Congratulations! Your Onboarding Verification is Complete";
        String body = String.format("""
            Hello %s,

            We are pleased to inform you that your onboarding documents and profile details have been approved by HR.

            Your account is now fully ACTIVE. You have full access to daily attendance punching, leave applications, payroll payslips, and internal directory.

            Welcome aboard!
            Priyex People HR Operations
            From: %s
            """, recipientName, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, "ONBOARDING_APPROVED");
    }

    @Override
    public boolean sendDemoConfirmation(String recipientEmail, String recipientName, String companyName, String inquiryRef, String scheduledWindow, String assignedConsultant) {
        String subject = "Priyex People Cloud — Interactive Live Demo Scheduled [" + inquiryRef + "]";
        String body = String.format("""
            Hello %s,

            Thank you for scheduling a live interactive demo of Priyex People Enterprise HRMS for %s.

            Demo Confirmation Details:
            ───────────────────────────────────────────────────────
            Booking Reference ID: %s
            Scheduled Window: %s
            Assigned Solutions Lead: %s
            Platform Access: https://priyex-hrms.vercel.app

            What to Expect During the 30-Minute Session:
            1. Real-time Multi-Tier Payroll & Statutory Tax calculation demonstration
            2. Biometric & Geofenced Time Attendance live clock-in
            3. Multi-tenant policy configuration tailored for %s
            4. Dedicated Q&A with your Solutions Lead

            If you need to reschedule or invite additional stakeholders from your team, simply reply to this email.

            Warm regards,
            Priyex Enterprise Solutions Group
            From: %s
            """, recipientName, companyName, inquiryRef, scheduledWindow, assignedConsultant, companyName, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, "DEMO_CONFIRMATION");
    }

    @Override
    public boolean sendInternalLeadAlert(String companyName, String fullName, String workEmail, String phoneNumber, String employeeCount, String interestModule, String scheduledWindow, String inquiryRef) {
        String subject = "🚨 New Priyex Lead & Demo Request: " + companyName + " [" + inquiryRef + "]";
        String body = String.format("""
            Hello Priyex Leadership & Sales Team,

            A new enterprise demo / sales consultation request has been submitted on priyex.com.

            Prospect Details:
            ───────────────────────────────────────────────────────
            • Company: %s
            • Contact Name: %s
            • Work Email: %s
            • Phone / WhatsApp: %s
            • Workforce Size: %s
            • Primary Interest: %s
            • Preferred Window: %s
            • Reference ID: %s

            Action Required:
            Please reach out to the prospect to confirm their interactive live demo session.

            Priyex People Cloud Automation
            """, companyName, fullName, workEmail,
                (phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : "N/A"),
                (employeeCount != null && !employeeCount.isBlank() ? employeeCount : "N/A"),
                (interestModule != null && !interestModule.isBlank() ? interestModule : "Full Suite"),
                (scheduledWindow != null && !scheduledWindow.isBlank() ? scheduledWindow : "Immediate"),
                inquiryRef);

        return dispatchEmail(alertRecipients, subject, body, "INTERNAL_LEAD_ALERT");
    }
}
