package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.model.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeMapper {

    Optional<Employee> findById(@Param("id") Long id, @Param("companyId") Long companyId);

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

    int softDelete(@Param("id") Long id, @Param("companyId") Long companyId);

    String generateEmployeeCode(@Param("companyId") Long companyId);
}
