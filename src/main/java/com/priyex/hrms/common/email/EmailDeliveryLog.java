package com.priyex.hrms.common.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDeliveryLog {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String templateCode;
    private String status; // PENDING, SENT, FAILED
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private Instant sentAt;
    private Instant createdAt;
}
