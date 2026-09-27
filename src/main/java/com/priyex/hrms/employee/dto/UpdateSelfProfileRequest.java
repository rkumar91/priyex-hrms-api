package com.priyex.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSelfProfileRequest {
    private String photoUrl;
    private String personalPhone;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
}
