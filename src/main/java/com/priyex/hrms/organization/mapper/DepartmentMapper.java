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

    @org.apache.ibatis.annotations.Insert("""
        INSERT INTO departments (company_id, code, name, description, is_active, created_at, updated_at)
        VALUES (#{companyId}, UPPER(#{code}), #{name}, #{description}, TRUE, NOW(), NOW())
        ON CONFLICT (company_id, code) DO NOTHING
    """)
    void insertDepartment(@Param("companyId") Long companyId,
                          @Param("code") String code,
                          @Param("name") String name,
                          @Param("description") String description);
}

