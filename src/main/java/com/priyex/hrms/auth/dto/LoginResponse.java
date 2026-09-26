package com.priyex.hrms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private AuthUserResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthUserResponse {
        private Long id;
        private String email;
        private String displayName;
        private String avatarUrl;
        private Long companyId;
        private Long employeeId;
        private Set<String> roles;
        private Set<String> permissions;
    }
}
