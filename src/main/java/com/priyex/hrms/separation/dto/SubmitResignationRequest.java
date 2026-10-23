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
public class SubmitResignationRequest {
    @NotBlank(message = "Reason category is required")
    private String reasonCategory;

    @NotBlank(message = "Reason explanation is required")
    private String reasonDetails;

    @NotNull(message = "Requested last working day is required")
    private LocalDate requestedLastWorkingDay;

    private String handoverNotes;
}
