package com.priyex.hrms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Long employeeId;
    private boolean active;
    private List<String> roles;
    private Instant lastLoginAt;
    private Instant createdAt;
}
