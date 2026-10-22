package com.priyex.hrms.common.whatsapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppDeliveryLog {
    private Long id;
    private String inquiryRef;
    private String recipientPhone;
    private String templateName;
    private String messageBody;
    private String provider; // META_CLOUD, TWILIO, MOCK
    private String messageId;
    private String status; // SENT, MOCK_SENT, FAILED
    private String errorMessage;
    private Instant sentAt;
    private Instant createdAt;
}
