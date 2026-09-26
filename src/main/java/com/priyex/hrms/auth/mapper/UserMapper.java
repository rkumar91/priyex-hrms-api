package com.priyex.hrms.auth.mapper;

import com.priyex.hrms.auth.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByEmail(@Param("email") String email);

    List<String> findRolesByUserId(@Param("userId") Long userId);

    List<String> findPermissionsByUserId(@Param("userId") Long userId);

    void insert(User user);

    void updateLastLogin(@Param("id") Long id, @Param("lastLoginAt") Instant lastLoginAt);

    void incrementFailedLoginAttempts(@Param("id") Long id);

    void resetFailedLoginAttempts(@Param("id") Long id);

    void lockUser(@Param("id") Long id, @Param("lockedUntil") Instant lockedUntil);

    void unlockUser(@Param("id") Long id);

    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    int countAll();

    List<User> findAll(@Param("offset") int offset, @Param("limit") int limit);
}
