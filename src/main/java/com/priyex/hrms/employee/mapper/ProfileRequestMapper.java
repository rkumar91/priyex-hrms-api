package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.model.ProfileRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProfileRequestMapper {

    Optional<ProfileRequest> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    List<ProfileRequest> findRequests(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("status") String status
    );

    int insert(ProfileRequest request);

    int updateStatus(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("reviewerId") Long reviewerId,
            @Param("reviewerNotes") String reviewerNotes
    );
}
