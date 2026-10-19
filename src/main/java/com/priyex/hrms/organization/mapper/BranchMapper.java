package com.priyex.hrms.organization.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BranchMapper {

    @Insert("""
        INSERT INTO branches (company_id, code, name, city, state, country, is_head_office, is_active, created_at, updated_at)
        VALUES (#{companyId}, #{code}, #{name}, #{city}, #{state}, #{country}, #{isHeadOffice}, TRUE, NOW(), NOW())
        ON CONFLICT DO NOTHING
    """)
    void insertBranch(@Param("companyId") Long companyId,
                      @Param("code") String code,
                      @Param("name") String name,
                      @Param("city") String city,
                      @Param("state") String state,
                      @Param("country") String country,
                      @Param("isHeadOffice") boolean isHeadOffice);

    @Select("""
        SELECT id
        FROM branches
        WHERE company_id = #{companyId}
        ORDER BY is_head_office DESC, id ASC
        LIMIT 1
    """)
    Long findDefaultBranchId(@Param("companyId") Long companyId);
}
