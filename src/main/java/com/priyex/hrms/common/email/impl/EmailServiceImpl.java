package com.priyex.hrms.common.email.impl;

import com.priyex.hrms.common.email.EmailDeliveryLog;
import com.priyex.hrms.common.email.EmailLogMapper;
import com.priyex.hrms.common.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final EmailLogMapper emailLogMapper;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.from:tech.priyex@gmail.com}")
    private String fromEmail;

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

        log.info("\n══════════════════ [EMAIL DISPATCH via {}] ══════════════════\nTo: {}\nSubject: {}\n\n{}\n══════════════════════════════════════════════════════════════",
                fromEmail, recipientEmail, subject, body);

        boolean sentReal = false;
        String errorMessage = null;

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(recipientEmail);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                sentReal = true;
                log.info("Live email successfully dispatched to {} from {}", recipientEmail, fromEmail);
            } catch (Exception e) {
                log.warn("Real SMTP dispatch failed (falling back to logged delivery): {}", e.getMessage());
                errorMessage = e.getMessage();
            }
        }

        EmailDeliveryLog deliveryLog = EmailDeliveryLog.builder()
                .recipientEmail(recipientEmail)
                .subject(subject)
                .templateCode("ONBOARDING_INVITE")
                .status(sentReal ? "SENT" : (errorMessage != null ? "FAILED" : "SENT_OFFLINE"))
                .errorMessage(errorMessage)
                .build();
        emailLogMapper.insert(deliveryLog);
        return sentReal;
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

        log.info("\n══════════════════ [EMAIL DISPATCH via {}] ══════════════════\nTo: {}\nSubject: {}\n\n{}\n══════════════════════════════════════════════════════════════",
                fromEmail, recipientEmail, subject, body);

        boolean sentReal = false;
        String errorMessage = null;

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(recipientEmail);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                sentReal = true;
                log.info("Live approval notification dispatched to {} from {}", recipientEmail, fromEmail);
            } catch (Exception e) {
                log.warn("Real SMTP dispatch failed for approval notification: {}", e.getMessage());
                errorMessage = e.getMessage();
            }
        }

        EmailDeliveryLog deliveryLog = EmailDeliveryLog.builder()
                .recipientEmail(recipientEmail)
                .subject(subject)
                .templateCode("ONBOARDING_APPROVED")
                .status(sentReal ? "SENT" : (errorMessage != null ? "FAILED" : "SENT_OFFLINE"))
                .errorMessage(errorMessage)
                .build();
        emailLogMapper.insert(deliveryLog);
        return sentReal;
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

        log.info("\n══════════════════ [EMAIL DISPATCH via {}] ══════════════════\nTo: {}\nSubject: {}\n\n{}\n══════════════════════════════════════════════════════════════",
                fromEmail, recipientEmail, subject, body);

        boolean sentReal = false;
        String errorMessage = null;

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(recipientEmail);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);
                sentReal = true;
                log.info("Live demo confirmation email dispatched to {} from {}", recipientEmail, fromEmail);
            } catch (Exception e) {
                log.warn("Real SMTP dispatch failed for demo confirmation: {}", e.getMessage());
                errorMessage = e.getMessage();
            }
        }

        EmailDeliveryLog deliveryLog = EmailDeliveryLog.builder()
                .recipientEmail(recipientEmail)
                .subject(subject)
                .templateCode("DEMO_CONFIRMATION")
                .status(sentReal ? "SENT" : (errorMessage != null ? "FAILED" : "SENT_OFFLINE"))
                .errorMessage(errorMessage)
                .build();
        emailLogMapper.insert(deliveryLog);
        return sentReal;
    }
}
