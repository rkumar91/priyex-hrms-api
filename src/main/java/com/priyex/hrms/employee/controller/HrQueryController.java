package com.priyex.hrms.employee.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.dto.CreateHrQueryRequest;
import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.dto.RespondHrQueryRequest;
import com.priyex.hrms.employee.dto.SendMessageRequest;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.model.HrQueryMessage;
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
@Tag(name = "HR Queries", description = "Endpoints for employee queries to HR, live support pool, and interactive messaging")
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
    @Operation(summary = "Employee sends a query to selected HR manager or into the general pool")
    public ResponseEntity<ApiResponse<HrQuery>> createQuery(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateHrQueryRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;
        String employeeName = (currentUser != null) ? currentUser.getDisplayName() : "Employee";

        HrQuery created = hrQueryService.createQuery(companyId, employeeId, request, employeeName);
        String msg = (request.getAssignedHrId() != null)
                ? "Query sent successfully to assigned HR Manager"
                : "Live request sent to available HR Pool. Connecting...";
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, msg));
    }

    @GetMapping("/pool")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Get unassigned live queries currently waiting in the pool")
    public ResponseEntity<ApiResponse<List<HrQuery>>> getPoolQueries(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<HrQuery> pool = hrQueryService.getPoolQueries(companyId);
        return ResponseEntity.ok(ApiResponse.success(pool));
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "HR Manager claims and connects to a query waiting in the pool")
    public ResponseEntity<ApiResponse<HrQuery>> claimQuery(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long hrUserId = (currentUser != null) ? currentUser.getId() : 1L;
        String hrDisplayName = (currentUser != null) ? currentUser.getDisplayName() : "HR Specialist";

        HrQuery claimed = hrQueryService.claimQuery(companyId, id, hrUserId, hrDisplayName);
        return ResponseEntity.ok(ApiResponse.success(claimed, "Successfully claimed and connected to query"));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark an active query as resolved")
    public ResponseEntity<ApiResponse<HrQuery>> resolveQuery(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        HrQuery resolved = hrQueryService.resolveQuery(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(resolved, "Query marked as resolved"));
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active query for current employee (if currently waiting or chatting)")
    public ResponseEntity<ApiResponse<HrQuery>> getActiveQuery(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;

        HrQuery active = hrQueryService.getActiveQueryForEmployee(companyId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(active));
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get message thread for a query")
    public ResponseEntity<ApiResponse<List<HrQueryMessage>>> getMessages(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<HrQueryMessage> messages = hrQueryService.getMessages(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a new message into the query chat thread")
    public ResponseEntity<ApiResponse<HrQueryMessage>> sendMessage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long senderUserId = (currentUser != null) ? currentUser.getId() : null;
        boolean isHrOrAdmin = currentUser != null && (
                currentUser.hasRole("SUPER_ADMIN") ||
                currentUser.hasRole("ADMIN") ||
                currentUser.hasRole("HR_ADMIN")
        );
        String senderType = isHrOrAdmin ? "HR" : "EMPLOYEE";
        String senderName = (currentUser != null && currentUser.getDisplayName() != null)
                ? currentUser.getDisplayName() : (isHrOrAdmin ? "HR Specialist" : "Employee");

        HrQueryMessage msg = hrQueryService.sendMessage(companyId, id, senderUserId, senderType, senderName, request.getMessage());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, "Message sent"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List queries (HR sees all/assigned queries, employee sees their own)")
    public ResponseEntity<ApiResponse<List<HrQuery>>> getQueries(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "assignedHrId", required = false) Long assignedHrId
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
            @PathVariable("id") Long id,
            @Valid @RequestBody RespondHrQueryRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        HrQuery updated = hrQueryService.respondToQuery(companyId, id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Response submitted successfully"));
    }
}
