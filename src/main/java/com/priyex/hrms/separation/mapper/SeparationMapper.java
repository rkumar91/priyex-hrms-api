package com.priyex.hrms.separation.mapper;

import com.priyex.hrms.separation.model.ExitClearance;
import com.priyex.hrms.separation.model.SeparationCase;
import com.priyex.hrms.separation.model.SeparationPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SeparationMapper {

    SeparationPolicy findPolicyByCompanyId(@Param("companyId") Long companyId);

    int insertPolicy(SeparationPolicy policy);

    int updatePolicy(SeparationPolicy policy);

    int insertSeparationCase(SeparationCase separationCase);

    int updateSeparationCase(SeparationCase separationCase);

    SeparationCase findSeparationById(@Param("id") Long id, @Param("companyId") Long companyId);

    SeparationCase findActiveSeparationByEmployeeId(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);

    List<SeparationCase> findSeparationCases(
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("employeeId") Long employeeId
    );

    int insertExitClearance(ExitClearance clearance);

    List<ExitClearance> findClearancesByCaseId(@Param("separationCaseId") Long separationCaseId);

    ExitClearance findClearanceById(@Param("id") Long id, @Param("companyId") Long companyId);

    int updateExitClearance(ExitClearance clearance);

    int deactivateUserByEmployeeId(@Param("employeeId") Long employeeId, @Param("companyId") Long companyId);

    int updateEmployeeExitStatus(
            @Param("employeeId") Long employeeId,
            @Param("companyId") Long companyId,
            @Param("status") String status,
            @Param("exitDate") LocalDate exitDate,
            @Param("exitReason") String exitReason
    );
}
