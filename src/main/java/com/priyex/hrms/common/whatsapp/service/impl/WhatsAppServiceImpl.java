package com.priyex.hrms.common.whatsapp.service.impl;

import com.priyex.hrms.common.whatsapp.mapper.WhatsAppLogMapper;
import com.priyex.hrms.common.whatsapp.model.WhatsAppDeliveryLog;
import com.priyex.hrms.common.whatsapp.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    private final WhatsAppLogMapper whatsAppLogMapper;

    @Value("${app.whatsapp.enabled:true}")
    private boolean enabled;

    @Value("${app.whatsapp.provider:META_CLOUD}")
    private String provider;

    @Value("${app.whatsapp.api-token:}")
    private String apiToken;

    @Value("${app.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${app.whatsapp.api-url:https://graph.facebook.com/v20.0}")
    private String apiUrl;

    @Value("${app.whatsapp.sales-alert-phone:+919901694683}")
    private String salesAlertPhone;

    @Value("${app.whatsapp.business-phone:+919901694683}")
    private String businessPhone;

    @Override
    @Async
    public boolean sendDemoBookingConfirmation(String phoneNumber,
                                               String recipientName,
                                               String companyName,
                                               String inquiryRef,
                                               String scheduledWindow,
                                               String assignedConsultant) {
        if (!enabled || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.info("WhatsApp dispatch skipped (disabled or no phone number provided).");
            return false;
        }

        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        String body = String.format("""
            🌟 *Priyex People Cloud — Demo Confirmation*
            
            Hello *%s*,
            
            Your live interactive demo session for *%s* is confirmed!
            
            📅 *Scheduled Window:* %s
            🔖 *Reference ID:* `%s`
            👤 *Assigned Solutions Specialist:* %s
            📞 *Official Priyex WhatsApp Helpline:* %s
            
            We've also dispatched your calendar invite and sandbox access credentials to your corporate email.
            
            To modify or reschedule, simply reply directly to this WhatsApp message or connect with our solutions desk at %s.
            
            _Priyex Enterprise Solutions Team_
            """, recipientName, companyName, scheduledWindow, inquiryRef, assignedConsultant, businessPhone, businessPhone);

        log.info("\n══════════════════ [WHATSAPP DISPATCH via {}] ══════════════════\nTo: {}\nInquiry Ref: {}\n\n{}\n════════════════════════════════════════════════════════════════",
                provider, normalizedPhone, inquiryRef, body);

        boolean isRealSent = false;
        String messageId = null;
        String errorMessage = null;

        // Dispatch real message if Meta credentials are configured
        if (!apiToken.isBlank() && !phoneNumberId.isBlank()) {
            try {
                RestClient restClient = RestClient.builder().baseUrl(apiUrl).build();
                Map<String, Object> payload = Map.of(
                        "messaging_product", "whatsapp",
                        "recipient_type", "individual",
                        "to", normalizedPhone,
                        "type", "text",
                        "text", Map.of("preview_url", false, "body", body)
                );

                String endpoint = "/" + phoneNumberId + "/messages";
                Map response = restClient.post()
                        .uri(endpoint)
                        .header("Authorization", "Bearer " + apiToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body(Map.class);

                log.info("Live WhatsApp message dispatched via Meta Cloud API. Response: {}", response);
                messageId = response != null ? String.valueOf(response.get("messages")) : "META-OK";
                isRealSent = true;
            } catch (Exception ex) {
                log.warn("Meta WhatsApp Cloud API dispatch encountered error (logging fallback): {}", ex.getMessage());
                errorMessage = ex.getMessage();
            }
        } else {
            log.info("WhatsApp Cloud API credentials not configured; logged message in MOCK simulation mode.");
            messageId = "SIMULATED-WA-" + System.currentTimeMillis();
        }

        WhatsAppDeliveryLog deliveryLog = WhatsAppDeliveryLog.builder()
                .inquiryRef(inquiryRef)
                .recipientPhone(normalizedPhone)
                .templateName("DEMO_BOOKING_CONFIRMATION")
                .messageBody(body)
                .provider(provider)
                .messageId(messageId)
                .status(isRealSent ? "SENT" : (errorMessage != null ? "FAILED" : "MOCK_SENT"))
                .errorMessage(errorMessage)
                .sentAt(Instant.now())
                .build();

        try {
            whatsAppLogMapper.insert(deliveryLog);
        } catch (Exception e) {
            log.warn("Failed to write WhatsApp audit record: {}", e.getMessage());
        }

        return isRealSent || errorMessage == null;
    }

    @Override
    @Async
    public boolean sendInternalSalesAlert(String recipientPhone,
                                          String companyName,
                                          String contactPerson,
                                          String email,
                                          String phone,
                                          String employeeCount,
                                          String interestModule,
                                          String scheduledWindow,
                                          String inquiryRef) {
        String targetPhone = (recipientPhone != null && !recipientPhone.isBlank()) ? recipientPhone : salesAlertPhone;
        if (targetPhone == null || targetPhone.isBlank()) {
            log.info("Internal sales alert skipped (no sales phone or group configured).");
            return false;
        }

        String normalizedPhone = normalizePhoneNumber(targetPhone);
        String body = String.format("""
            🚨 *Priyex Enterprise Lead Alert*
            
            • *Company:* %s
            • *Contact:* %s
            • *Email:* %s
            • *Phone:* %s
            • *Workforce:* %s
            • *Module:* %s
            • *Requested Slot:* %s
            • *Reference ID:* `%s`
            
            _Priyex Enterprise Sales Automation_
            """, companyName, contactPerson,
                (email != null && !email.isBlank() ? email : "N/A"),
                (phone != null && !phone.isBlank() ? phone : "N/A"),
                (employeeCount != null && !employeeCount.isBlank() ? employeeCount : "N/A"),
                (interestModule != null && !interestModule.isBlank() ? interestModule : "General"),
                (scheduledWindow != null && !scheduledWindow.isBlank() ? scheduledWindow : "Immediate"),
                inquiryRef);

        log.info("\n══════════════════ [STATIC SALES GROUP ALERT via {}] ══════════════════\nTo Sales Group/Phone: {}\nInquiry Ref: {}\n\n{}\n══════════════════════════════════════════════════════════════════════",
                provider, normalizedPhone, inquiryRef, body);

        boolean isRealSent = false;
        String messageId = null;
        String errorMessage = null;

        if (!apiToken.isBlank() && !phoneNumberId.isBlank()) {
            try {
                RestClient restClient = RestClient.builder().baseUrl(apiUrl).build();
                Map<String, Object> payload = Map.of(
                        "messaging_product", "whatsapp",
                        "recipient_type", "individual",
                        "to", normalizedPhone,
                        "type", "text",
                        "text", Map.of("preview_url", false, "body", body)
                );

                String endpoint = "/" + phoneNumberId + "/messages";
                Map response = restClient.post()
                        .uri(endpoint)
                        .header("Authorization", "Bearer " + apiToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body(Map.class);

                log.info("Live Sales Group WhatsApp message dispatched via Meta Cloud API. Response: {}", response);
                messageId = response != null ? String.valueOf(response.get("messages")) : "SALES-META-OK";
                isRealSent = true;
            } catch (Exception ex) {
                log.warn("Sales Group WhatsApp dispatch error: {}", ex.getMessage());
                errorMessage = ex.getMessage();
            }
        } else {
            messageId = "SIMULATED-SALES-ALERT-" + System.currentTimeMillis();
        }

        WhatsAppDeliveryLog deliveryLog = WhatsAppDeliveryLog.builder()
                .inquiryRef(inquiryRef)
                .recipientPhone(normalizedPhone)
                .templateName("STATIC_SALES_GROUP_ALERT")
                .messageBody(body)
                .provider(provider)
                .messageId(messageId)
                .status(isRealSent ? "SENT" : (errorMessage != null ? "FAILED" : "MOCK_SENT"))
                .errorMessage(errorMessage)
                .sentAt(Instant.now())
                .build();

        try {
            whatsAppLogMapper.insert(deliveryLog);
        } catch (Exception ignored) {
        }
        return isRealSent || errorMessage == null;
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        // If 10 digits Indian mobile, prepend 91
        if (clean.length() == 10) {
            return "91" + clean;
        }
        return clean;
    }
}
