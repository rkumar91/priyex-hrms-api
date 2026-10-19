package com.priyex.hrms.organization.mapper;

import com.priyex.hrms.organization.model.CompanySetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompanySettingMapper {

    @Select("""
        SELECT id, company_id, setting_key, setting_value, description, created_at, updated_at
        FROM company_settings
        WHERE company_id = #{companyId}
        ORDER BY setting_key ASC
    """)
    List<CompanySetting> findAllByCompanyId(@Param("companyId") Long companyId);

    @Select("""
        SELECT id, company_id, setting_key, setting_value, description, created_at, updated_at
        FROM company_settings
        WHERE company_id = #{companyId} AND setting_key = #{settingKey}
        LIMIT 1
    """)
    CompanySetting findByCompanyIdAndKey(@Param("companyId") Long companyId, @Param("settingKey") String settingKey);

    @Insert("""
        INSERT INTO company_settings (company_id, setting_key, setting_value, description, updated_at)
        VALUES (#{companyId}, #{settingKey}, #{settingValue}, #{description}, NOW())
        ON CONFLICT (company_id, setting_key) DO UPDATE
        SET setting_value = EXCLUDED.setting_value,
            description = COALESCE(EXCLUDED.description, company_settings.description),
            updated_at = NOW()
    """)
    int upsert(@Param("companyId") Long companyId,
               @Param("settingKey") String settingKey,
               @Param("settingValue") String settingValue,
               @Param("description") String description);
}
