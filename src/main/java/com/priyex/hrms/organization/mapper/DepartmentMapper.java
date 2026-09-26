package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.dto.DepartmentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    @Select("""
        SELECT d.id, d.code, d.name, d.description,
               COUNT(e.id)::INTEGER AS employee_count
        FROM departments d
        LEFT JOIN employees e ON e.department_id = d.id AND e.status != 'EXITED'
        WHERE d.company_id = #{companyId} AND d.is_active = TRUE
        GROUP BY d.id, d.code, d.name, d.description
        ORDER BY d.name ASC
    """)
    List<DepartmentDTO> findAllByCompanyId(@Param("companyId") Long companyId);
}
