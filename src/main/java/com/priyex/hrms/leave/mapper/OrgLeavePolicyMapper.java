package com.priyex.hrms.leave.mapper;

import com.priyex.hrms.leave.model.OrgLeavePolicy;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OrgLeavePolicyMapper {

    @Select("""
        SELECT id, company_id AS companyId, leave_code AS leaveCode, leave_name AS leaveName,
               annual_days AS annualDays, is_paid AS isPaid,
               carry_forward_allowed AS carryForwardAllowed,
               max_carry_forward_days AS maxCarryForwardDays,
               description, is_active AS isActive,
               created_at AS createdAt, updated_at AS updatedAt
        FROM org_leave_policies
        WHERE company_id = #{companyId}
        ORDER BY id ASC
    """)
    List<OrgLeavePolicy> findAllByCompanyId(@Param("companyId") Long companyId);

    @Select("""
        SELECT id, company_id AS companyId, leave_code AS leaveCode, leave_name AS leaveName,
               annual_days AS annualDays, is_paid AS isPaid,
               carry_forward_allowed AS carryForwardAllowed,
               max_carry_forward_days AS maxCarryForwardDays,
               description, is_active AS isActive,
               created_at AS createdAt, updated_at AS updatedAt
        FROM org_leave_policies
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    Optional<OrgLeavePolicy> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    @Insert("""
        INSERT INTO org_leave_policies (
            company_id, leave_code, leave_name, annual_days, is_paid,
            carry_forward_allowed, max_carry_forward_days, description, is_active, created_at, updated_at
        ) VALUES (
            #{companyId}, #{leaveCode}, #{leaveName}, #{annualDays}, #{isPaid},
            #{carryForwardAllowed}, #{maxCarryForwardDays}, #{description}, #{isActive}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrgLeavePolicy policy);

    @Update("""
        UPDATE org_leave_policies
        SET leave_name = #{leaveName},
            annual_days = #{annualDays},
            is_paid = #{isPaid},
            carry_forward_allowed = #{carryForwardAllowed},
            max_carry_forward_days = #{maxCarryForwardDays},
            description = #{description},
            is_active = #{isActive},
            updated_at = NOW()
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int update(OrgLeavePolicy policy);

    @Delete("""
        DELETE FROM org_leave_policies
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int delete(@Param("id") Long id, @Param("companyId") Long companyId);
}
