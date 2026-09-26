package com.priyex.hrms.organization.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.organization.dto.DepartmentDTO;
import com.priyex.hrms.organization.mapper.DepartmentMapper;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
@Tag(name = "Organization Management", description = "Endpoints for company, departments, and branches")
public class OrganizationController {

    private final DepartmentMapper departmentMapper;

    @GetMapping("/departments")
    @Operation(summary = "Get active departments with employee counts")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartments(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<DepartmentDTO> departments = departmentMapper.findAllByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
}
