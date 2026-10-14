package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignationDTO {
    private Long id;
    private Long companyId;
    private String name;
    private Integer level;
    private String description;
}
