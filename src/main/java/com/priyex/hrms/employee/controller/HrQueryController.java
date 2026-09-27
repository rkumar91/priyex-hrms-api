package com.priyex.hrms.employee.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.service.HrQueryService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hr-queries")
@RequiredArgsConstructor
@Tag(name = "HR Queries", description = "Endpoints for employee queries to HR and HR responses")
public class HrQueryController {

    private final HrQueryService hrQueryService;

    @GetMapping("/personnel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of HR Personnel for employee dropdown selection")
    public ResponseEntity<ApiResponse<List<HrPersonnelDto>>> getHrPersonnelList(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<HrPersonnelDto> list = hrQueryService.getHrPersonnelList(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Employee sends a query to selected HR manager")
    public ResponseEntity<ApiResponse<HrQuery>> createQuery(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateHrQueryRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;

        HrQuery created = hrQueryService.createQuery(companyId, employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Query sent successfully to HR Manager"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List queries (HR sees all/assigned queries, employee sees their own)")
    public ResponseEntity<ApiResponse<List<HrQuery>>> getQueries(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedHrId
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        boolean isHrOrAdmin = currentUser != null && (
                currentUser.hasRole("SUPER_ADMIN") ||
                currentUser.hasRole("ADMIN") ||
                currentUser.hasRole("HR_ADMIN")
        );

        Long employeeIdFilter = null;
        if (!isHrOrAdmin) {
            employeeIdFilter = (currentUser != null && currentUser.getEmployeeId() != null)
                    ? currentUser.getEmployeeId() : 1L;
        }

        List<HrQuery> list = hrQueryService.getQueries(companyId, employeeIdFilter, assignedHrId, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "HR Manager responds to employee query")
    public ResponseEntity<ApiResponse<HrQuery>> respondToQuery(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody RespondHrQueryRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        HrQuery updated = hrQueryService.respondToQuery(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Response submitted successfully"));
    }
}
