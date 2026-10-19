package com.priyex.hrms.organization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySetting {
    private Long id;
    private Long companyId;
    private String settingKey;
    private String settingValue;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
