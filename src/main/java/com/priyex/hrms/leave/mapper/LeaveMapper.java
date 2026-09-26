package com.priyex.hrms.leave.mapper;

import com.priyex.hrms.leave.model.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LeaveMapper {

    List<LeaveRequest> findAllByCompanyId(@Param("companyId") Long companyId);

    Optional<LeaveRequest> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    int insert(LeaveRequest leaveRequest);

    int updateStatus(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("approverComment") String approverComment,
            @Param("approvedBy") Long approvedBy
    );
}
