package com.priyex.hrms.auth.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginAuditMapper {

    @Insert("INSERT INTO login_audit (user_id, email, action, ip_address, user_agent, details) " +
            "VALUES (#{userId}, #{email}, #{action}, #{ipAddress}, #{userAgent}, #{details})")
    void insert(@Param("userId") Long userId,
                @Param("email") String email,
                @Param("action") String action,
                @Param("ipAddress") String ipAddress,
                @Param("userAgent") String userAgent,
                @Param("details") String details);
}
