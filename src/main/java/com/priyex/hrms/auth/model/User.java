package com.priyex.hrms.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private Long companyId;
    private Long employeeId;
    private String email;
    private String passwordHash;
    private String displayName;
    private String avatarUrl;
    private String phone;
    private boolean active;
    private boolean locked;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;
    private boolean mustChangePassword;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
    private int version;
}
