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
public class UploadDocumentRequest {

    @NotBlank(message = "Document type is required")
    private String documentType; // AADHAAR_CARD, PAN_CARD, PASSPORT, DEGREE_CERTIFICATE, PREVIOUS_RELIEVING, CANCELLED_CHEQUE, OTHER

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotBlank(message = "Document file data is required")
    private String fileData; // Base64 data URL

    private String mimeType;
    private Long fileSize;
}
