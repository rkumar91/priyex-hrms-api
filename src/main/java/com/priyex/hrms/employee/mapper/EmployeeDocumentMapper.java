package com.priyex.hrms.employee.mapper;

import com.priyex.hrms.employee.model.EmployeeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeDocumentMapper {

    List<EmployeeDocument> findByEmployeeId(@Param("employeeId") Long employeeId);

    Optional<EmployeeDocument> findById(@Param("id") Long id);

    int insert(EmployeeDocument document);

    int delete(@Param("id") Long id, @Param("employeeId") Long employeeId);
}
