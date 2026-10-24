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
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserMapper userMapper;
    private final LoginAuditMapper loginAuditMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AppProperties appProperties;
    private final EmployeeMapper employeeMapper;
    private final com.priyex.hrms.organization.mapper.CompanyMapper companyMapper;

    public AuthServiceImpl(
            UserMapper userMapper,
            LoginAuditMapper loginAuditMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService,
            AppProperties appProperties,
            EmployeeMapper employeeMapper,
            com.priyex.hrms.organization.mapper.CompanyMapper companyMapper
    ) {
        super();
        this.userMapper = userMapper;
        this.loginAuditMapper = loginAuditMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.appProperties = appProperties;
        this.employeeMapper = employeeMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userMapper.findByEmail(request.getEmail());

        // Don't reveal whether user exists
        if (user == null) {
            safeAudit(null, request.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "User not found");
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if account is locked
        if (user.isLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
                safeAudit(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "Account locked");
                throw new LockedException("Account is locked. Please try again later.");
            }
            // Lock expired, unlock
            userMapper.unlockUser(user.getId());
        }

        // Check if account is active
        if (!user.isActive()) {
            safeAudit(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent, "Account inactive");
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

        // Multi-tenant organization verification: Validate assigned organization is active
        if (user.getCompanyId() != null) {
            com.priyex.hrms.organization.dto.CompanyDto company = companyMapper.findById(user.getCompanyId());
            if (company != null && Boolean.FALSE.equals(company.getIsActive())) {
                safeAudit(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent,
                        "Organization workspace deactivated");
                throw new BadCredentialsException("Your organization workspace has been suspended or deactivated. Please contact support.");
            }
        }

        // Multi-tenant workspace validation: Ensure user belongs to the requested workspace
        List<String> userRoles = userMapper.findRolesByUserId(user.getId());
        boolean isSuperAdmin = userRoles.contains("SUPER_ADMIN");

        if (!isSuperAdmin) {
            Long targetCompanyId = request.getCompanyId();
            if (targetCompanyId == null && request.getCompanyCode() != null && !request.getCompanyCode().isBlank()) {
                var comp = companyMapper.findByIdentifier(request.getCompanyCode().trim());
                if (comp != null) {
                    targetCompanyId = comp.getId();
                } else {
                    safeAudit(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent,
                            "Organization code not found: " + request.getCompanyCode());
                    throw new BadCredentialsException("Organization / Company Code '" + request.getCompanyCode().trim()
                            + "' not found in directory. Please verify your organization identifier.");
                }
            }

            if (targetCompanyId != null && user.getCompanyId() != null && !targetCompanyId.equals(user.getCompanyId())) {
                var selectedCompany = companyMapper.findById(targetCompanyId);
                var userActualCompany = companyMapper.findById(user.getCompanyId());

                String selectedName = (selectedCompany != null && selectedCompany.getBrandName() != null)
                        ? selectedCompany.getBrandName() : "the selected workspace";
                String actualName = (userActualCompany != null && userActualCompany.getBrandName() != null)
                        ? userActualCompany.getBrandName() : "their registered organization";

                safeAudit(user.getId(), user.getEmail(), "LOGIN_FAILED", ipAddress, userAgent,
                        "Workspace mismatch: user registered under " + actualName + " (id=" + user.getCompanyId()
                        + ") attempted login via workspace " + selectedName + " (id=" + targetCompanyId + ")");

                throw new BadCredentialsException("Account " + user.getEmail() + " belongs to " + actualName
                        + " (" + (userActualCompany != null ? userActualCompany.getCode() : "registered org") + "), not "
                        + selectedName + " (" + request.getCompanyCode() + "). Please switch your Organization Code to sign in.");
            }
        }

        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        safeAudit(user.getId(), user.getEmail(), "LOGIN_SUCCESS", ipAddress, userAgent, null);

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
        String displayName = principal.getDisplayName();
        String avatarUrl = null;

        try {
            Long companyId = principal.getCompanyId() != null ? principal.getCompanyId() : 1L;
            Employee emp = null;
            if (principal.getEmployeeId() != null) {
                emp = employeeMapper.findById(principal.getEmployeeId(), companyId).orElse(null);
            }
            if (emp == null && principal.getId() != null) {
                emp = employeeMapper.findByUserId(principal.getId(), companyId).orElse(null);
            }
            if (emp != null) {
                if (emp.getFirstName() != null && !emp.getFirstName().isBlank()) {
                    String full = (emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim();
                    if (!full.isBlank()) {
                        displayName = full;
                    }
                }
                if (emp.getPhotoUrl() != null && !emp.getPhotoUrl().isBlank()) {
                    avatarUrl = emp.getPhotoUrl();
                }
            }
        } catch (Exception e) {
            log.warn("Could not sync employee profile into auth user response", e);
        }

        com.priyex.hrms.organization.dto.CompanyDto companyDto = null;
        if (principal.getCompanyId() != null) {
            try {
                companyDto = companyMapper.findById(principal.getCompanyId());
            } catch (Exception e) {
                log.warn("Could not load company details for user {}", principal.getEmail(), e);
            }
        }

        return LoginResponse.AuthUserResponse.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .companyId(principal.getCompanyId())
                .company(companyDto)
                .employeeId(principal.getEmployeeId())
                .mustChangePassword(principal.isMustChangePassword())
                .roles(principal.getRoles())
                .permissions(principal.getPermissions())
                .build();
    }

    private void safeAudit(Long userId, String email, String action, String ip, String ua, String details) {
        try {
            String safeIp = (ip != null && ip.length() > 45) ? ip.substring(0, 45) : ip;
            String safeUa = (ua != null && ua.length() > 500) ? ua.substring(0, 500) : ua;
            String safeDetails = (details != null && details.length() > 500) ? details.substring(0, 500) : details;
            loginAuditMapper.insert(userId, email, action, safeIp, safeUa, safeDetails);
        } catch (Exception ex) {
            log.warn("Failed to insert login audit record: {}", ex.getMessage());
        }
    }
}
