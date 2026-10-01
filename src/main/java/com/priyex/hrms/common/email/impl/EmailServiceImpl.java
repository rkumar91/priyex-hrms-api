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
    public void sendOnboardingInvite(String recipientEmail, String recipientName, String tempPassword, String loginUrl) {
        String subject = "Welcome to Priyex People — Your Onboarding Credentials";
        String body = String.format("""
            Hello %s,

            Welcome to Priyex People Enterprise HRMS!

            Your onboarding profile has been initiated. Please use the temporary credentials below to log in:

            Portal URL: %s
            Username / Email: %s
            Temporary Password: %s

            *Important*: Upon first login, you will be required to change your temporary password to a permanent secure password and complete your onboarding profile submission.

            Warm regards,
            Priyex People HR Operations
            From: %s
            """, recipientName, loginUrl, recipientEmail, tempPassword, fromEmail);

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
    }

    @Override
    public void sendOnboardingApprovalNotification(String recipientEmail, String recipientName) {
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
    }
}
