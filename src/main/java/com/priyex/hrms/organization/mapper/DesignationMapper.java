package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.dto.DesignationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DesignationMapper {

    @Select("""
        SELECT id, company_id, name, level, description
        FROM designations
        WHERE company_id = #{companyId} AND is_active = TRUE
        ORDER BY level ASC, name ASC
    """)
    List<DesignationDTO> findAllByCompanyId(@Param("companyId") Long companyId);

    @org.apache.ibatis.annotations.Insert("""
        INSERT INTO designations (company_id, name, level, description, is_active, created_at, updated_at)
        VALUES (#{companyId}, #{name}, #{level}, #{description}, TRUE, NOW(), NOW())
        ON CONFLICT (company_id, name) DO NOTHING
    """)
    void insertDesignation(@Param("companyId") Long companyId,
                           @Param("name") String name,
                           @Param("level") Integer level,
                           @Param("description") String description);
}
