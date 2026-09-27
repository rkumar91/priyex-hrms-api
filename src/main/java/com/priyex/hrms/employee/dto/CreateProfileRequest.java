package com.priyex.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {
    @NotBlank(message = "Request type is required")
    private String requestType; // BANK_ACCOUNT, CONTACT_NAME, EMAIL, OTHER

    @NotBlank(message = "Field name is required")
    private String fieldName;

    private String currentValue;

    @NotBlank(message = "Requested value is required")
    private String requestedValue;

    private String reason;
}
