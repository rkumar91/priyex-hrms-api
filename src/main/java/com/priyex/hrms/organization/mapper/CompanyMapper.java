package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.dto.CompanyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompanyMapper {

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, is_active
        FROM companies
        WHERE is_active = TRUE
        ORDER BY id ASC
    """)
    List<CompanyDto> findAllActiveCompanies();

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, is_active
        FROM companies
        WHERE id = #{id}
    """)
    CompanyDto findById(@Param("id") Long id);
}
