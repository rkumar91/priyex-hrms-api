package com.priyex.hrms.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeCtcRequest {

    @NotNull(message = "Annual CTC is required")
    @DecimalMin(value = "10000.00", message = "Annual CTC must be at least ₹10,000")
    private BigDecimal annualCtc;
}
