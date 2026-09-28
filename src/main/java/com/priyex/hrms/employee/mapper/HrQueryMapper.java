package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.model.HrQuery;
import com.priyex.hrms.employee.model.HrQueryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface HrQueryMapper {

    Optional<HrQuery> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    List<HrQuery> findQueries(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId,
            @Param("assignedHrId") Long assignedHrId,
            @Param("status") String status
    );

    List<HrQuery> findPoolQueries(@Param("companyId") Long companyId);

    int insert(HrQuery query);

    int claimQuery(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("assignedHrId") Long assignedHrId
    );

    int respondToQuery(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("response") String response,
            @Param("status") String status
    );

    int resolveQuery(
            @Param("id") Long id,
            @Param("companyId") Long companyId
    );

    Optional<HrQuery> findActiveQueryForEmployee(
            @Param("companyId") Long companyId,
            @Param("employeeId") Long employeeId
    );

    List<HrPersonnelDto> findHrPersonnelList(@Param("companyId") Long companyId);

    // Message Thread Methods
    List<HrQueryMessage> findMessagesByQueryId(@Param("queryId") Long queryId);

    int insertMessage(HrQueryMessage message);
}
