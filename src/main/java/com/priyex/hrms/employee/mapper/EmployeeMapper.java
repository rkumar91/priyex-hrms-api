package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.model.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    Optional<Employee> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    Optional<Employee> findByUserId(@Param("userId") Long userId, @Param("companyId") Long companyId);

    List<Employee> searchEmployees(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            @Param("departmentId") Long departmentId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countEmployees(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            @Param("departmentId") Long departmentId,
            @Param("status") String status
    );

    int insert(Employee employee);

    int update(Employee employee);

    int updateSelfProfile(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("req") com.priyex.hrms.employee.dto.UpdateSelfProfileRequest req
    );

    int updateDirectField(
            @Param("id") Long id,
            @Param("companyId") Long companyId,
            @Param("columnName") String columnName,
            @Param("value") String value
    );

    int softDelete(@Param("id") Long id, @Param("companyId") Long companyId);

    String generateEmployeeCode(@Param("companyId") Long companyId);

    List<com.priyex.hrms.auth.dto.EmployeeUserRoleDto> findEmployeeUserRoles(@Param("companyId") Long companyId);

    int updateEmployeeCtc(@Param("id") Long id, @Param("companyId") Long companyId, @Param("annualCtc") java.math.BigDecimal annualCtc);

    int linkUserId(@Param("employeeId") Long employeeId, @Param("userId") Long userId, @Param("companyId") Long companyId);
}
