package com.priyex.hrms.security;

import com.priyex.hrms.auth.mapper.UserMapper;
import com.priyex.hrms.auth.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Loads user details from database for Spring Security authentication.
 * Fetches roles and permissions in a single efficient query.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return buildPrincipal(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
        return buildPrincipal(user);
    }

    private UserPrincipal buildPrincipal(User user) {
        Set<String> roles = new HashSet<>(userMapper.findRolesByUserId(user.getId()));
        Set<String> permissions = new HashSet<>(userMapper.findPermissionsByUserId(user.getId()));

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .displayName(user.getDisplayName())
                .employeeId(user.getEmployeeId())
                .companyId(user.getCompanyId())
                .active(user.isActive())
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
