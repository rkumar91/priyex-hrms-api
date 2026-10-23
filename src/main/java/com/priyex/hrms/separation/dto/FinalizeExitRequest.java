package com.priyex.hrms.separation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeExitRequest {
    private String exitInterviewNotes;
    private Boolean deactivateUser; // Defaults to true
    private String remarks;
}
