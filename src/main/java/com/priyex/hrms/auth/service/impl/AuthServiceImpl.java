package com.priyex.hrms.auth.service.impl;

import com.priyex.hrms.auth.dto.ChangePasswordRequest;
import com.priyex.hrms.auth.dto.LoginRequest;
import com.priyex.hrms.auth.dto.LoginResponse;
import com.priyex.hrms.auth.mapper.LoginAuditMapper;
import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import com.priyex.hrms.auth.service.AuthService;
import com.priyex.hrms.common.exception.BadRequestException;
import com.priyex.hrms.config.AppProperties;
import com.priyex.hrms.security.CustomUserDetailsService;
import com.priyex.hrms.security.JwtTokenProvider;
import com.priyex.hrms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserMapper userMapper;
    private final LoginAuditMapper loginAuditMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userMapper.findByEmail(request.getEmail());

        // Don't reveal whether user exists
        if (user == null) {
            loginAuditMapper.insert(null, request.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "User not found");
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if account is locked
        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
                loginAuditMapper.insert(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "Account locked");
                throw new LockedException("Account is locked. Please try again later.");
            }
            // Lock expired, unlock
            userMapper.unlockUser(user.getId());
        }

        // Check if account is active
        if (!user.isActive()) {
            loginAuditMapper.insert(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "Account inactive");
            throw new BadCredentialsException("Invalid email or password");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user, ipAddress, userAgent);
            throw new BadCredentialsException("Invalid email or password");
        }

        // Success — reset failed attempts, update last login
        userMapper.resetFailedLoginAttempts(user.getId());
        userMapper.updateLastLogin(user.getId(), Instant.now());

        // Build principal and generate tokens
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserById(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        loginAuditMapper.insert(user.getId(), user.getEmail(), "LOGIN_SUCCESS", ipAddress, userAgent, null);

        log.info("User logged in: {} from {}", user.getEmail(), ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(appProperties.getJwt().getAccessTokenExpirationMs() / 1000)
                .user(buildAuthUserResponse(principal))
                .build();
    }

    @Override
    @Transactional
    public void logout(UserPrincipal principal) {
        loginAuditMapper.insert(principal.getId(), principal.getEmail(), "LOGOUT", null, null, null);
        log.info("User logged out: {}", principal.getEmail());
    }

    @Override
    public LoginResponse.AuthUserResponse getCurrentUser(UserPrincipal principal) {
        return buildAuthUserResponse(principal);
    }

    @Override
    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        User user = userMapper.findById(principal.getId());
        if (user == null) {
            throw new BadRequestException("User not found");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        String newHash = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(user.getId(), newHash);

        log.info("Password changed for user: {}", user.getEmail());
    }

    private void handleFailedLogin(User user, String ipAddress, String userAgent) {
        int attempts = user.getFailedLoginAttempts() + 1;
        userMapper.incrementFailedLoginAttempts(user.getId());

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            Instant lockUntil = Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES);
            userMapper.lockUser(user.getId(), lockUntil);
            loginAuditMapper.insert(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent,
                    "Account locked after " + attempts + " failed attempts");
            log.warn("Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        } else {
            loginAuditMapper.insert(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent,
                    "Failed attempt " + attempts + "/" + MAX_FAILED_ATTEMPTS);
        }
    }

    private LoginResponse.AuthUserResponse buildAuthUserResponse(UserPrincipal principal) {
        return LoginResponse.AuthUserResponse.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .displayName(principal.getDisplayName())
                .avatarUrl(null) // loaded separately if needed
                .companyId(principal.getCompanyId())
                .employeeId(principal.getEmployeeId())
                .mustChangePassword(principal.isMustChangePassword())
                .roles(principal.getRoles())
                .permissions(principal.getPermissions())
                .build();
    }
}
