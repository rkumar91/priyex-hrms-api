package com.priyex.hrms.employee.dto;

import com.priyex.hrms.employee.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePublicResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String workEmail;
    private String workPhone;
    private String departmentName;
    private String designationName;
    private String branchName;
    private String reportingManagerName;
    private String employmentType;
    private String status;
    private LocalDate joiningDate;

    public static EmployeePublicResponse from(Employee e) {
        if (e == null) return null;
        return EmployeePublicResponse.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .photoUrl(e.getPhotoUrl())
                .workEmail(e.getWorkEmail())
                .workPhone(e.getWorkPhone())
                .departmentName(e.getDepartmentName())
                .designationName(e.getDesignationName())
                .branchName(e.getBranchName())
                .reportingManagerName(e.getReportingManagerName())
                .employmentType(e.getEmploymentType())
                .status(e.getStatus())
                .joiningDate(e.getJoiningDate())
                .build();
    }
}
