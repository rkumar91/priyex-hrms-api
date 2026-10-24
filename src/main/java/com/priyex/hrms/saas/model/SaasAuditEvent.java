package com.priyex.hrms.saas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaasAuditEvent {
    private Long id;
    private String actorEmail;
    private String actionType;
    private String targetEntity;
    private String targetId;
    private String summary;
    private String detailsJson;
    private String ipAddress;
    private Instant createdAt;
}
