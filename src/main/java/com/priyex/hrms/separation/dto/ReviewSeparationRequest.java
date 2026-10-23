package com.priyex.hrms.separation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSeparationRequest {
    @NotBlank(message = "Action must be APPROVE or REJECT")
    private String action; // APPROVE, REJECT

    private LocalDate approvedLastWorkingDay;
    private Boolean isNoticeWaived;
    private String remarks;
}
