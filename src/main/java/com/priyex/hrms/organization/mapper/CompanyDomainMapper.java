package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.model.CompanyDomain;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompanyDomainMapper {

    @Select("""
        SELECT id, company_id, domain_name, is_primary, is_verified, is_shared, created_at, updated_at
        FROM company_domains
        WHERE LOWER(domain_name) = LOWER(#{domainName})
        LIMIT 1
    """)
    CompanyDomain findByDomainName(@Param("domainName") String domainName);

    @Select("""
        SELECT id, company_id, domain_name, is_primary, is_verified, is_shared, created_at, updated_at
        FROM company_domains
        WHERE company_id = #{companyId}
        ORDER BY is_primary DESC, domain_name ASC
    """)
    List<CompanyDomain> findDomainsByCompanyId(@Param("companyId") Long companyId);

    @Insert("""
        INSERT INTO company_domains (company_id, domain_name, is_primary, is_verified, is_shared, created_at, updated_at)
        VALUES (#{companyId}, LOWER(#{domainName}), #{isPrimary}, #{isVerified}, #{isShared}, NOW(), NOW())
        ON CONFLICT (company_id, domain_name) DO UPDATE
        SET is_primary = EXCLUDED.is_primary,
            is_verified = EXCLUDED.is_verified,
            updated_at = NOW()
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CompanyDomain domain);

    @Delete("""
        DELETE FROM company_domains
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int deleteByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

    @Select("""
        SELECT COUNT(*)
        FROM company_domains
        WHERE LOWER(domain_name) = LOWER(#{domainName})
    """)
    int countByDomainName(@Param("domainName") String domainName);
}
