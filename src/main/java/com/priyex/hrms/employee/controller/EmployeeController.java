package com.priyex.hrms.employee.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.common.response.PagedResponse;
import com.priyex.hrms.employee.dto.CreateEmployeeRequest;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.service.EmployeeService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for employee master, onboarding, update, and export")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAuthority('emp.view_all') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Search & list employees with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<Employee>>> getEmployees(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        PagedResponse<Employee> result = employeeService.getEmployees(companyId, query, departmentId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('emp.view_all') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get employee profile by ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Employee employee = employeeService.getEmployeeById(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('emp.create') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Onboard a new employee")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        Employee created = employeeService.createEmployee(companyId, actorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Employee onboarded successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('emp.edit') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update employee details")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Employee updated = employeeService.updateEmployee(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Employee updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('emp.manage_lifecycle') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Deactivate employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        employeeService.deleteEmployee(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deactivated successfully"));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('report.export') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Export employee directory to CSV")
    public ResponseEntity<byte[]> exportEmployeesCsv(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        String csv = employeeService.exportEmployeesCsv(companyId);
        byte[] bytes = csv.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
