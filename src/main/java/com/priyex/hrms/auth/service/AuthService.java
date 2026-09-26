package com.priyex.hrms.auth.service;

import com.priyex.hrms.auth.dto.ChangePasswordRequest;
import com.priyex.hrms.auth.dto.LoginRequest;
import com.priyex.hrms.auth.dto.LoginResponse;
import com.priyex.hrms.security.UserPrincipal;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(UserPrincipal principal);

    LoginResponse.AuthUserResponse getCurrentUser(UserPrincipal principal);

    void changePassword(UserPrincipal principal, ChangePasswordRequest request);
}
