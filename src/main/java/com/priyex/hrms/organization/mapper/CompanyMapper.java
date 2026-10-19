package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.dto.CompanyDto;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompanyMapper {

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, logo_url, is_active
        FROM companies
        WHERE is_active = TRUE
        ORDER BY id ASC
    """)
    List<CompanyDto> findAllActiveCompanies();

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, logo_url, is_active
        FROM companies
        ORDER BY id ASC
    """)
    List<CompanyDto> findAllClients();

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, logo_url, is_active
        FROM companies
        WHERE id = #{id}
    """)
    CompanyDto findById(@Param("id") Long id);

    @Select("""
        SELECT id, code, legal_name, brand_name, industry, timezone, currency_code,
               registration_no, tax_id, email, phone, city, state, country, logo_url, is_active
        FROM companies
        WHERE UPPER(code) = UPPER(#{code})
        LIMIT 1
    """)
    CompanyDto findByCode(@Param("code") String code);

    @Insert("""
        INSERT INTO companies (code, legal_name, brand_name, industry, timezone, currency_code,
                               locale, registration_no, tax_id, email, phone, city, state, country, logo_url, is_active, created_at, updated_at)
        VALUES (UPPER(#{code}), #{legalName}, #{brandName}, #{industry},
                COALESCE(#{timezone}, 'Asia/Kolkata'), COALESCE(#{currencyCode}, 'INR'),
                'en-IN', #{registrationNo}, #{taxId}, #{email}, #{phone}, #{city}, #{state},
                COALESCE(#{country}, 'India'), #{logoUrl}, #{isActive}, NOW(), NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCompany(CompanyDto company);

    @Update("""
        UPDATE companies
        SET logo_url = #{logoUrl},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateCompanyLogo(@Param("id") Long id, @Param("logoUrl") String logoUrl);

    @Update("""
        UPDATE companies
        SET is_active = #{isActive},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateCompanyStatus(@Param("id") Long id, @Param("isActive") boolean isActive);

    @Select("""
        SELECT COUNT(*)
        FROM employees
        WHERE company_id = #{companyId}
    """)
    int countEmployeesByCompanyId(@Param("companyId") Long companyId);

    @Select("""
        SELECT u.email
        FROM users u
        INNER JOIN user_roles ur ON ur.user_id = u.id
        INNER JOIN roles r ON r.id = ur.role_id
        WHERE u.company_id = #{companyId}
          AND r.name IN ('ADMIN', 'SUPER_ADMIN')
        ORDER BY u.id ASC
        LIMIT 1
    """)
    String findPrimaryAdminEmailByCompanyId(@Param("companyId") Long companyId);
}
