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
}
