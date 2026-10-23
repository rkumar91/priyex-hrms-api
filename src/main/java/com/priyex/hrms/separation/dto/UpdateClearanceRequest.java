package com.priyex.hrms.separation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClearanceRequest {
    @NotBlank(message = "Status is required (CLEARED, WAIVED, FLAGGED, PENDING)")
    private String status;

    private BigDecimal deductionAmount;
    private String remarks;
}
