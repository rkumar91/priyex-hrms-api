package com.priyex.hrms.auth.controller;

import com.priyex.hrms.auth.dto.AdminUserResponse;
import com.priyex.hrms.auth.dto.EmployeeUserRoleDto;
import com.priyex.hrms.auth.dto.UpdateUserRoleRequest;
import com.priyex.hrms.auth.dto.UpdateUserRolesRequest;
import com.priyex.hrms.auth.dto.UpdateUserStatusRequest;
import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.organization.mapper.CompanySettingMapper;
import com.priyex.hrms.organization.model.CompanySetting;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.priyex.hrms.organization.mapper.CompanyMapper;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User & Role Governance", description = "Endpoints for managing user accounts and assigning roles like HR Manager, Admin, Employee")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('COMPANY_ADMIN')")
public class AdminUserController {

    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompanySettingMapper companySettingMapper;
    private final CompanyMapper companyMapper;

    public AdminUserController(
            UserMapper userMapper,
            EmployeeMapper employeeMapper,
            PasswordEncoder passwordEncoder,
            CompanySettingMapper companySettingMapper,
            CompanyMapper companyMapper
    ) {
        super();
        this.userMapper = userMapper;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.companySettingMapper = companySettingMapper;
        this.companyMapper = companyMapper;
    }

    @GetMapping
    @Operation(summary = "List registered system users with their roles scoped to organization")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers(
            @CurrentUser UserPrincipal currentUser
    ) {
        boolean isSuperAdmin = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().contains("SUPER_ADMIN");
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;

        List<User> users;
        if (isSuperAdmin) {
            users = userMapper.findAll(0, 500);
        } else {
            users = userMapper.findByCompanyId(companyId, 0, 500);
        }

        Map<Long, String> companyNameMap = new HashMap<>();
        List<AdminUserResponse> result = users.stream().map(u -> {
            List<String> roles = userMapper.findRolesByUserId(u.getId());
            String compName = null;
            if (u.getCompanyId() != null) {
                compName = companyNameMap.computeIfAbsent(u.getCompanyId(), cid -> {
                    try {
                        var c = companyMapper.findById(cid);
                        return c != null ? (c.getBrandName() != null ? c.getBrandName() : c.getLegalName()) : "Company #" + cid;
                    } catch (Exception e) {
                        return "Company #" + cid;
                    }
                });
            }

            return AdminUserResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .displayName(u.getDisplayName())
                    .avatarUrl(u.getAvatarUrl())
                    .employeeId(u.getEmployeeId())
                    .companyId(u.getCompanyId())
                    .companyName(compName)
                    .active(u.isActive())
                    .roles(roles)
                    .lastLoginAt(u.getLastLoginAt())
                    .createdAt(u.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/employees-roles")
    @Operation(summary = "Admin side - User Role table management for every employee")
    public ResponseEntity<ApiResponse<List<EmployeeUserRoleDto>>> listEmployeeUserRoles(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<EmployeeUserRoleDto> list = employeeMapper.findEmployeeUserRoles(companyId);
        for (EmployeeUserRoleDto item : list) {
            if (item.getUserId() != null) {
                List<String> roles = userMapper.findRolesByUserId(item.getUserId());
                item.setRoles(roles);
                item.setPrimaryRole(roles.isEmpty() ? "NO_ROLE" : roles.get(0));
            } else {
                item.setRoles(List.of());
                item.setPrimaryRole("NO_ACCOUNT");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/employees/{employeeId}/roles")
    @Transactional
    @Operation(summary = "Admin assigns multiple roles to a specific employee")
    public ResponseEntity<ApiResponse<EmployeeUserRoleDto>> updateEmployeeRoles(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<String> requestedRoles = request.getRoles().stream()
                .map(r -> r.toUpperCase().trim())
                .distinct()
                .collect(Collectors.toList());

        List<Long> roleIds = new ArrayList<>();
        for (String rName : requestedRoles) {
            Long rId = userMapper.findRoleIdByName(rName);
            if (rId == null) {
                throw new BadRequestException("Unknown role: " + rName + ". Valid roles: SUPER_ADMIN, ADMIN, HR_ADMIN, HR_EXECUTIVE, MANAGER, EMPLOYEE");
            }
            roleIds.add(rId);
        }

        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        Long targetUserId = employee.getUserId();

        if (targetUserId == null) {
            User existingUser = userMapper.findByEmail(employee.getWorkEmail());
            if (existingUser != null) {
                targetUserId = existingUser.getId();
            } else {
                User newUser = User.builder()
                        .companyId(companyId)
                        .employeeId(employeeId)
                        .email(employee.getWorkEmail())
                        .displayName(employee.getFirstName() + " " + employee.getLastName())
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .active(true)
                        .mustChangePassword(true)
                        .createdBy(actorId)
                        .build();
                userMapper.insert(newUser);
                targetUserId = newUser.getId();
            }
            employeeMapper.linkUserId(employeeId, targetUserId, companyId);
        }

        // Anti-lockout protection: Admin cannot revoke their own admin status
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(targetUserId)) {
            boolean hasAdminRole = requestedRoles.contains("ADMIN") || requestedRoles.contains("SUPER_ADMIN") || requestedRoles.contains("COMPANY_ADMIN");
            if (!hasAdminRole) {
                throw new BadRequestException("Anti-Lockout Protection: You cannot remove your own Administrator role.");
            }
        }

        userMapper.deleteUserRoles(targetUserId);
        for (Long rId : roleIds) {
            userMapper.insertUserRole(targetUserId, rId, actorId);
        }

        List<String> updatedRoles = userMapper.findRolesByUserId(targetUserId);
        EmployeeUserRoleDto result = EmployeeUserRoleDto.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .departmentName(employee.getDepartmentName())
                .designationName(employee.getDesignationName())
                .workEmail(employee.getWorkEmail())
                .userId(targetUserId)
                .userEmail(employee.getWorkEmail())
                .userActive(true)
                .roles(updatedRoles)
                .primaryRole(updatedRoles.isEmpty() ? "NO_ROLE" : updatedRoles.get(0))
                .build();

        return ResponseEntity.ok(ApiResponse.success(result, "Employee roles updated successfully to: " + String.join(", ", updatedRoles)));
    }

    @PutMapping("/employees/{employeeId}/role")
    @Transactional
    @Operation(summary = "Legacy single-role assignment endpoint for an employee")
    public ResponseEntity<ApiResponse<EmployeeUserRoleDto>> updateEmployeeRole(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UpdateUserRolesRequest multiRequest = new UpdateUserRolesRequest(List.of(request.getRole()));
        return updateEmployeeRoles(currentUser, employeeId, multiRequest);
    }

    @PutMapping("/{id}/roles")
    @Transactional
    @Operation(summary = "Admin assigns multiple roles to a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRoles(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Anti-cross-tenant guard: Tenant Admin can only manage users within their own organization
        boolean isSuperAdmin = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().contains("SUPER_ADMIN");
        if (!isSuperAdmin && currentUser != null && currentUser.getCompanyId() != null) {
            if (user.getCompanyId() != null && !user.getCompanyId().equals(currentUser.getCompanyId())) {
                throw new AccessDeniedException("Access denied: You cannot manage user accounts belonging to another organization.");
            }
        }

        List<String> requestedRoles = request.getRoles().stream()
                .map(r -> r.toUpperCase().trim())
                .distinct()
                .collect(Collectors.toList());

        List<Long> roleIds = new ArrayList<>();
        for (String rName : requestedRoles) {
            Long rId = userMapper.findRoleIdByName(rName);
            if (rId == null) {
                throw new BadRequestException("Unknown role: " + rName + ". Valid roles: SUPER_ADMIN, ADMIN, HR_ADMIN, HR_EXECUTIVE, MANAGER, EMPLOYEE");
            }
            roleIds.add(rId);
        }

        // Anti-lockout guard
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
            boolean hasAdminRole = requestedRoles.contains("ADMIN") || requestedRoles.contains("SUPER_ADMIN") || requestedRoles.contains("COMPANY_ADMIN");
            if (!hasAdminRole) {
                throw new BadRequestException("Anti-Lockout Protection: You cannot remove your own Administrator role.");
            }
        }

        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        userMapper.deleteUserRoles(id);
        for (Long rId : roleIds) {
            userMapper.insertUserRole(id, rId, actorId);
        }

        List<String> roles = userMapper.findRolesByUserId(id);
        AdminUserResponse response = AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .employeeId(user.getEmployeeId())
                .active(user.isActive())
                .roles(roles)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "User roles updated successfully to: " + String.join(", ", roles)));
    }

    @PutMapping("/{id}/role")
    @Transactional
    @Operation(summary = "Legacy single-role assignment endpoint for a user account")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UpdateUserRolesRequest multiRequest = new UpdateUserRolesRequest(List.of(request.getRole()));
        return updateUserRoles(currentUser, id, multiRequest);
    }

    @PutMapping("/{id}/status")
    @Transactional
    @Operation(summary = "Admin activates or deactivates a user login")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestBody UpdateUserStatusRequest request
    ) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Anti-cross-tenant guard
        boolean isSuperAdmin = currentUser != null && currentUser.getRoles() != null && currentUser.getRoles().contains("SUPER_ADMIN");
        if (!isSuperAdmin && currentUser != null && currentUser.getCompanyId() != null) {
            if (user.getCompanyId() != null && !user.getCompanyId().equals(currentUser.getCompanyId())) {
                throw new AccessDeniedException("Access denied: You cannot manage user accounts belonging to another organization.");
            }
        }

        // Anti-lockout guard
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id) && !request.isActive()) {
            throw new BadRequestException("Anti-Lockout Protection: You cannot deactivate your own active login account.");
        }

        userMapper.updateUserStatus(id, request.isActive());
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated to " + (request.isActive() ? "ACTIVE" : "INACTIVE")));
    }

    // ── Role-Based Feature Policy Matrix Endpoints ──

    @GetMapping("/roles/policies")
    @Operation(summary = "Fetch company-scoped Role Feature Policy Matrix")
    public ResponseEntity<ApiResponse<String>> getRolePolicies(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        CompanySetting setting = companySettingMapper.findByCompanyIdAndKey(companyId, "role_feature_policies");
        String configJson = setting != null && setting.getSettingValue() != null ? setting.getSettingValue() : "";
        return ResponseEntity.ok(ApiResponse.success(configJson));
    }

    @PutMapping("/roles/policies")
    @Transactional
    @Operation(summary = "Update company-scoped Role Feature Policy Matrix")
    public ResponseEntity<ApiResponse<String>> updateRolePolicies(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody Map<String, Object> payload
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        String matrixJson = (payload != null && payload.containsKey("matrix")) ? String.valueOf(payload.get("matrix")) : "{}";
        companySettingMapper.upsert(companyId, "role_feature_policies", matrixJson, "Role-based feature enablement matrix");
        return ResponseEntity.ok(ApiResponse.success(matrixJson, "Role feature policies updated successfully"));
    }
}
