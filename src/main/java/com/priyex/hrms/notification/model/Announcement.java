package com.priyex.hrms.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    private Long id;
    private Long companyId;
    private String title;
    private String body;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private String targetAudience; // ALL, DEPARTMENT, BRANCH, ROLE
    private Long targetEntityId;
    private Instant publishedAt;
    private Instant expiresAt;
    private Boolean isPublished;
    private Boolean requiresAck;
    private Long createdBy;
    private String creatorName;
    private Instant createdAt;
    private Instant updatedAt;

    // User-specific state
    private Boolean isRead;
    private Instant acknowledgedAt;
}
