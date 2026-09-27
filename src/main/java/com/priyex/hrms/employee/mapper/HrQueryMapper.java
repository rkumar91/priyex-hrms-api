package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.dto.HrPersonnelDto;
import com.priyex.hrms.employee.model.HrQuery;
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

    int insert(HrQuery query);

    int respondToQuery(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("response") String response,
            @Param("status") String status
    );

    List<HrPersonnelDto> findHrPersonnelList(@Param("companyId") Long companyId);
}
