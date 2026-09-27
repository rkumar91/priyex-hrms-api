package com.priyex.hrms.auth.controller;

import com.priyex.hrms.auth.dto.ChangePasswordRequest;
import com.priyex.hrms.auth.dto.LoginRequest;
import com.priyex.hrms.auth.dto.LoginResponse;
import com.priyex.hrms.auth.service.AuthService;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, token refresh, password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns authenticated user profile with roles and permissions")
    public ResponseEntity<ApiResponse<LoginResponse.AuthUserResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal) {
        LoginResponse.AuthUserResponse user = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/password/change")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
