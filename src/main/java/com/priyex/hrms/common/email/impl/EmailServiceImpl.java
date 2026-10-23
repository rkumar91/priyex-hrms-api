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
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
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

    @Value("${app.mail.from:${spring.mail.from:tech.priyex@gmail.com}}")
    private String fromEmail;

    @Value("${app.platform-url:https://www.priyex.com}")
    private String platformUrl;

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
        return dispatchEmail(recipientEmail, subject, body, null, templateCode);
    }

    @SuppressWarnings("null")
    private boolean dispatchEmail(String recipientEmail, String subject, String body, String htmlBody, String templateCode) {
        log.info(
                "\n══════════════════ [EMAIL DISPATCH via Brevo/Resend/SMTP] ══════════════════\nTo: {}\nSubject: {}\n\n{}\n══════════════════════════════════════════════════════════════",
                recipientEmail, subject, body);

        boolean sentReal = false;
        String errorMessage = null;

        // 1. Try Brevo HTTPS REST API (Port 443 — sends reliably from tech.priyex@gmail.com on Render)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            RestClient brevoClient = RestClient.builder().baseUrl("https://api.brevo.com/v3").build();
            List<String> recipients = Arrays.stream(recipientEmail.split(","))
                    .map(r -> r.trim())
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String singleRecipient : recipients) {
                try {
                    Map<String, Object> payload;
                    if (htmlBody != null && !htmlBody.isBlank()) {
                        payload = Map.of(
                                "sender", Map.of("name", brevoSenderName, "email", fromEmail),
                                "to", List.of(Map.of("email", singleRecipient)),
                                "subject", subject,
                                "htmlContent", htmlBody,
                                "textContent", body != null ? body : "");
                    } else {
                        payload = Map.of(
                                "sender", Map.of("name", brevoSenderName, "email", fromEmail),
                                "to", List.of(Map.of("email", singleRecipient)),
                                "subject", subject,
                                "textContent", body != null ? body : "");
                    }

                    Map<?, ?> response = brevoClient.post()
                            .uri("/smtp/email")
                            .header("api-key", brevoApiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .body(Map.class);

                    log.info("Live email dispatched via Brevo HTTPS API to {}: messageId={}", singleRecipient,
                            response != null ? response.get("messageId") : "OK");
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
                    .map(r -> r.trim())
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (String singleRecipient : recipients) {
                try {
                    Map<String, Object> payload;
                    if (htmlBody != null && !htmlBody.isBlank()) {
                        payload = Map.of(
                                "from", resendFromEmail,
                                "to", List.of(singleRecipient),
                                "subject", subject,
                                "html", htmlBody,
                                "text", body != null ? body : "");
                    } else {
                        payload = Map.of(
                                "from", resendFromEmail,
                                "to", List.of(singleRecipient),
                                "subject", subject,
                                "text", body != null ? body : "");
                    }

                    Map<?, ?> response = restClient.post()
                            .uri("/emails")
                            .header("Authorization", "Bearer " + resendApiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .body(Map.class);

                    log.info("Live email dispatched via Resend HTTPS API to {}: id={}", singleRecipient,
                            response != null ? response.get("id") : "OK");
                    sentReal = true;
                } catch (Exception ex) {
                    log.warn("Resend dispatch to {} failed: {}", singleRecipient, ex.getMessage());
                    errorMessage = "Resend (" + singleRecipient + "): " + ex.getMessage();
                }
            }
        }

        // 3. Fallback to JavaMailSender (SMTP) if Resend not configured or failed
        if (!sentReal) {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender != null) {
                try {
                    String[] recipients = recipientEmail.split(",");
                    for (String r : recipients) {
                        String clean = r.trim();
                        if (!clean.isEmpty()) {
                            if (htmlBody != null && !htmlBody.isBlank()) {
                                try {
                                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                                    helper.setFrom(fromEmail);
                                    helper.setTo(clean);
                                    helper.setSubject(subject);
                                    helper.setText(body != null ? body : "", htmlBody);
                                    mailSender.send(mimeMessage);
                                } catch (Exception mimeEx) {
                                    SimpleMailMessage msg = new SimpleMailMessage();
                                    msg.setFrom(fromEmail);
                                    msg.setTo(clean);
                                    msg.setSubject(subject);
                                    msg.setText(body != null ? body : "");
                                    mailSender.send(msg);
                                }
                            } else {
                                SimpleMailMessage msg = new SimpleMailMessage();
                                msg.setFrom(fromEmail);
                                msg.setTo(clean);
                                msg.setSubject(subject);
                                msg.setText(body != null ? body : "");
                                mailSender.send(msg);
                            }
                        }
                    }
                    sentReal = true;
                    log.info("Live email dispatched via SMTP to {} from {}", recipientEmail, fromEmail);
                    errorMessage = null;
                } catch (Exception e) {
                    log.warn("SMTP dispatch failed: {}", e.getMessage());
                    if (errorMessage == null)
                        errorMessage = "SMTP: " + e.getMessage();
                }
            }
        }

        // 4. Record delivery log
        try {
            EmailDeliveryLog deliveryLog = EmailDeliveryLog.builder()
                    .recipientEmail(recipientEmail != null && recipientEmail.length() > 200
                            ? recipientEmail.substring(0, 197) + "..."
                            : recipientEmail)
                    .subject(subject != null && subject.length() > 300 ? subject.substring(0, 297) + "..." : subject)
                    .templateCode(templateCode)
                    .status(sentReal ? "SENT" : (errorMessage != null ? "FAILED" : "SENT_OFFLINE"))
                    .errorMessage(
                            errorMessage != null && errorMessage.length() > 500 ? errorMessage.substring(0, 497) + "..."
                                    : errorMessage)
                    .build();
            emailLogMapper.insert(deliveryLog);
        } catch (Exception e) {
            log.warn("Failed to write email delivery log: {}", e.getMessage());
        }

        return sentReal;
    }

    @Override
    public boolean sendOnboardingInvite(String recipientEmail, String workEmail, String recipientName,
            String tempPassword, String loginUrl) {
        String subject = "Welcome to Priyex People — Your Onboarding Credentials";
        String body = String.format(
                """
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
                        """,
                recipientName, loginUrl, workEmail, tempPassword, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, "ONBOARDING_INVITE");
    }

    @Override
    public boolean sendOnboardingApprovalNotification(String recipientEmail, String recipientName) {
        String subject = "Congratulations! Your Onboarding Verification is Complete";
        String body = String.format(
                """
                        Hello %s,

                        We are pleased to inform you that your onboarding documents and profile details have been approved by HR.

                        Your account is now fully ACTIVE. You have full access to daily attendance punching, leave applications, payroll payslips, and internal directory.

                        Welcome aboard!
                        Priyex People HR Operations
                        From: %s
                        """,
                recipientName, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, "ONBOARDING_APPROVED");
    }

    @Override
    public boolean sendDemoConfirmation(String recipientEmail, String recipientName, String companyName,
            String inquiryRef, String scheduledWindow, String assignedConsultant) {
        String subject = "Priyex People Cloud — Interactive Live Demo Scheduled [" + inquiryRef + "]";
        
        String body = String.format("""
                Hello %s,

                Thank you for scheduling a live interactive demo of Priyex People Enterprise HRMS for %s.

                Demo Confirmation Details:
                ───────────────────────────────────────────────────────
                Booking Reference ID: %s
                Scheduled Window: %s
                Assigned Solutions Lead: %s
                Platform Access: %s

                What to Expect During the 30-Minute Session:
                1. Real-time Multi-Tier Payroll & Statutory Tax calculation demonstration
                2. Biometric & Geofenced Time Attendance live clock-in
                3. Multi-tenant policy configuration tailored for %s
                4. Dedicated Q&A with your Solutions Lead

                If you need to reschedule or invite additional stakeholders from your team, simply reply to this email.

                Warm regards,
                Priyex Enterprise Solutions Group
                From: %s
                """, recipientName, companyName, inquiryRef, scheduledWindow, assignedConsultant, platformUrl, companyName,
                fromEmail);

        String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Priyex People Cloud - Demo Confirmation</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b;">
                  <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f1f5f9; padding: 24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0;">
                          <!-- Header Banner -->
                          <tr>
                            <td style="background-color: #0f172a; padding: 28px 32px; border-bottom: 3px solid #10b981; text-align: left;">
                              <span style="font-size: 20px; font-weight: 800; letter-spacing: -0.5px; color: #ffffff;">PRIYEX <span style="color: #10b981;">PEOPLE</span> CLOUD</span>
                              <div style="font-size: 11px; color: #94a3b8; margin-top: 4px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600;">Enterprise Multi-Tenant HRMS &amp; Payroll</div>
                            </td>
                          </tr>
                          <!-- Body Content -->
                          <tr>
                            <td style="padding: 32px 32px 24px 32px;">
                              <!-- Badge -->
                              <div style="display: inline-block; background-color: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 9999px; padding: 4px 14px; margin-bottom: 16px;">
                                <span style="color: #047857; font-size: 11px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;">&#10003; Live Demo Session Confirmed</span>
                              </div>
                              <h2 style="margin: 0 0 12px 0; color: #0f172a; font-size: 22px; font-weight: 800; line-height: 1.3;">
                                You're All Set, %s!
                              </h2>
                              <p style="margin: 0 0 24px 0; color: #475569; font-size: 14px; line-height: 1.6;">
                                A tailored 1-on-1 walkthrough of the Priyex HRMS SaaS Platform has been reserved for <strong style="color: #0f172a;">%s</strong>. Our solutions team is excited to demonstrate how our intelligent workforce architecture accelerates your operations.
                              </p>

                              <!-- Booking Details Card -->
                              <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; margin-bottom: 24px;">
                                <tr>
                                  <td style="padding: 16px 20px; border-bottom: 1px solid #e2e8f0;">
                                    <span style="color: #64748b; font-size: 12px; font-weight: 500;">Booking Reference</span><br>
                                    <span style="display: inline-block; font-family: monospace; font-size: 13px; font-weight: 700; color: #4338ca; background-color: #eef2ff; border: 1px solid #c7d2fe; padding: 2px 8px; border-radius: 6px; margin-top: 4px;">%s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding: 14px 20px; border-bottom: 1px solid #e2e8f0;">
                                    <span style="color: #64748b; font-size: 12px; font-weight: 500;">Scheduled Window</span><br>
                                    <span style="color: #0f172a; font-size: 14px; font-weight: 700;">&#128197; %s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding: 14px 20px; border-bottom: 1px solid #e2e8f0;">
                                    <span style="color: #64748b; font-size: 12px; font-weight: 500;">Assigned Solutions Lead</span><br>
                                    <span style="color: #0f172a; font-size: 14px; font-weight: 700;">&#128100; %s</span>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="padding: 14px 20px;">
                                    <span style="color: #64748b; font-size: 12px; font-weight: 500;">Platform Access</span><br>
                                    <a href="%s" target="_blank" style="color: #4f46e5; font-size: 14px; font-weight: 700; text-decoration: underline;">%s</a>
                                  </td>
                                </tr>
                              </table>

                              <!-- Call to Action Button -->
                              <div style="text-align: center; margin: 28px 0;">
                                <a href="%s" target="_blank" style="background-color: #0f172a; color: #ffffff; text-decoration: none; padding: 14px 32px; border-radius: 10px; font-size: 14px; font-weight: 700; display: inline-block; letter-spacing: 0.2px;">
                                  Access Priyex Platform &rarr;
                                </a>
                              </div>

                              <!-- Agenda Section -->
                              <div style="background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 24px;">
                                <h4 style="margin: 0 0 12px 0; color: #0f172a; font-size: 13px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px;">What to Expect During Your Session</h4>
                                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="font-size: 13px; color: #334155; line-height: 1.6;">
                                  <tr>
                                    <td style="padding: 4px 0; vertical-align: top; width: 22px; color: #10b981; font-weight: bold;">1.</td>
                                    <td style="padding: 4px 0;">Real-time Multi-Tier Payroll &amp; Statutory Tax calculation demonstration</td>
                                  </tr>
                                  <tr>
                                    <td style="padding: 4px 0; vertical-align: top; width: 22px; color: #10b981; font-weight: bold;">2.</td>
                                    <td style="padding: 4px 0;">Biometric &amp; Geofenced Time Attendance live clock-in</td>
                                  </tr>
                                  <tr>
                                    <td style="padding: 4px 0; vertical-align: top; width: 22px; color: #10b981; font-weight: bold;">3.</td>
                                    <td style="padding: 4px 0;">Multi-tenant policy configuration tailored for %s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding: 4px 0; vertical-align: top; width: 22px; color: #10b981; font-weight: bold;">4.</td>
                                    <td style="padding: 4px 0;">Dedicated Q&amp;A with your Solutions Lead</td>
                                  </tr>
                                </table>
                              </div>

                              <!-- Rescheduling note -->
                              <p style="margin: 0 0 20px 0; font-size: 13px; color: #64748b; line-height: 1.5;">
                                If you need to reschedule or invite additional stakeholders from your team, simply reply to this email or reach us at <a href="mailto:tech.priyex@gmail.com" style="color: #4f46e5; text-decoration: underline;">tech.priyex@gmail.com</a>.
                              </p>

                              <!-- Sign-off -->
                              <p style="margin: 0; font-size: 13px; color: #334155;">
                                Warm regards,<br>
                                <strong style="color: #0f172a;">Priyex Enterprise Solutions Group</strong><br>
                                <span style="color: #94a3b8; font-size: 12px;">From: %s</span>
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td style="background-color: #f8fafc; padding: 20px 32px; border-top: 1px solid #e2e8f0; text-align: center;">
                              <p style="margin: 0 0 6px 0; font-size: 11px; color: #94a3b8;">
                                &copy; 2026 Priyex People Cloud. Enterprise Multi-Tenant SaaS. All rights reserved.
                              </p>
                              <p style="margin: 0; font-size: 11px; color: #64748b;">
                                Zero spam guarantee. Enterprise-grade encryption standard.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """, recipientName, companyName, inquiryRef, scheduledWindow, assignedConsultant, platformUrl,
                platformUrl, platformUrl, companyName, fromEmail);

        return dispatchEmail(recipientEmail, subject, body, htmlBody, "DEMO_CONFIRMATION");
    }

    @Override
    public boolean sendInternalLeadAlert(String companyName, String fullName, String workEmail, String phoneNumber,
            String employeeCount, String interestModule, String scheduledWindow, String inquiryRef) {
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
