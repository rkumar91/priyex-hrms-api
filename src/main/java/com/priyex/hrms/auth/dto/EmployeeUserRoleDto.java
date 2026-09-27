package com.priyex.hrms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUserRoleDto {
    private Long employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String departmentName;
    private String designationName;
    private String workEmail;
    private Long userId;
    private String userEmail;
    private boolean userActive;
    private List<String> roles;
    private String primaryRole;
}
