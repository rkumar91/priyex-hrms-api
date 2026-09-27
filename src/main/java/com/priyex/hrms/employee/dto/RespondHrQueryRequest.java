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
public class RespondHrQueryRequest {
    @NotBlank(message = "Response is required")
    private String response;

    private String status; // RESOLVED, IN_PROGRESS, CLOSED
}
