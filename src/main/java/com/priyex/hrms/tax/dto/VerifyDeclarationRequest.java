package com.priyex.hrms.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDeclarationRequest {
    private String status; // VERIFIED or REJECTED
    private String hrNotes;
}
