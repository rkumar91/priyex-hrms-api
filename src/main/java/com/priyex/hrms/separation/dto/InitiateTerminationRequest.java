package com.priyex.hrms.separation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateTerminationRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Separation type is required (e.g. TERMINATION, MUTUAL_SEPARATION, END_OF_CONTRACT)")
    private String caseType;

    @NotBlank(message = "Reason category is required")
    private String reasonCategory;

    @NotBlank(message = "Reason details are required")
    private String reasonDetails;

    @NotNull(message = "Effective last working day is required")
    private LocalDate lastWorkingDay;

    private Boolean isNoticeWaived;
    private String severanceNotes;
    private String handoverNotes;
}
