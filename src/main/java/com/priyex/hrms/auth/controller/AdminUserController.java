package com.priyex.hrms.auth.controller;

import com.priyex.hrms.auth.dto.AdminUserResponse;
import com.priyex.hrms.auth.dto.EmployeeUserRoleDto;
import com.priyex.hrms.auth.dto.UpdateUserRoleRequest;
import com.priyex.hrms.auth.dto.UpdateUserStatusRequest;
import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User & Role Governance", description = "Endpoints for managing user accounts and assigning roles like HR Manager, Admin, Employee")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class AdminUserController {

    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "List all registered system users with their roles")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> listUsers() {
        List<User> users = userMapper.findAll(0, 500);
        List<AdminUserResponse> result = users.stream().map(u -> {
            List<String> roles = userMapper.findRolesByUserId(u.getId());
            return AdminUserResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .displayName(u.getDisplayName())
                    .avatarUrl(u.getAvatarUrl())
                    .employeeId(u.getEmployeeId())
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

    @PutMapping("/employees/{employeeId}/role")
    @Transactional
    @Operation(summary = "Admin assigns or updates role for a specific employee")
    public ResponseEntity<ApiResponse<EmployeeUserRoleDto>> updateEmployeeRole(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("employeeId") Long employeeId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Employee employee = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        String roleName = request.getRole().toUpperCase().trim();
        Long roleId = userMapper.findRoleIdByName(roleName);
        if (roleId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unknown role: " + roleName + ". Valid roles: SUPER_ADMIN, HR_ADMIN, EMPLOYEE, MANAGER"));
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

        userMapper.deleteUserRoles(targetUserId);
        userMapper.insertUserRole(targetUserId, roleId, actorId);

        List<String> roles = userMapper.findRolesByUserId(targetUserId);
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
                .roles(roles)
                .primaryRole(roleName)
                .build();

        return ResponseEntity.ok(ApiResponse.success(result, "Employee role updated successfully to " + roleName));
    }

    @PutMapping("/{id}/role")
    @Transactional
    @Operation(summary = "Admin assigns or updates a user role (e.g. HR_ADMIN, EMPLOYEE, SUPER_ADMIN)")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRole(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        String roleName = request.getRole().toUpperCase().trim();
        Long roleId = userMapper.findRoleIdByName(roleName);
        if (roleId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unknown role: " + roleName + ". Valid roles: SUPER_ADMIN, HR_ADMIN, EMPLOYEE, MANAGER"));
        }

        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        userMapper.deleteUserRoles(id);
        userMapper.insertUserRole(id, roleId, actorId);

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

        return ResponseEntity.ok(ApiResponse.success(response, "User role updated successfully to " + roleName));
    }

    @PutMapping("/{id}/status")
    @Transactional
    @Operation(summary = "Admin activates or deactivates a user login")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateUserStatusRequest request
    ) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userMapper.updateUserStatus(id, request.isActive());
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated to " + (request.isActive() ? "ACTIVE" : "INACTIVE")));
    }
}
