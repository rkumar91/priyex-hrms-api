package com.priyex.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrPersonnelDto {
    private Long id;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String role;
}
