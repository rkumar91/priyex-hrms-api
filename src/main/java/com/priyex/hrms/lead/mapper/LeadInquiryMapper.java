package com.priyex.hrms.lead.mapper;

import com.priyex.hrms.lead.model.LeadInquiry;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LeadInquiryMapper {

    @Insert("""
        INSERT INTO lead_inquiries (
            inquiry_ref, full_name, work_email, company_name, phone_number,
            employee_count, interest_module, inquiry_type, preferred_date,
            preferred_time_slot, message, status, assigned_consultant,
            email_status, whatsapp_status, created_at, updated_at
        ) VALUES (
            #{inquiryRef}, #{fullName}, #{workEmail}, #{companyName}, #{phoneNumber},
            #{employeeCount}, #{interestModule}, #{inquiryType}, #{preferredDate},
            #{preferredTimeSlot}, #{message}, #{status}, #{assignedConsultant},
            #{emailStatus}, #{whatsappStatus}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LeadInquiry lead);

    @Select("SELECT * FROM lead_inquiries WHERE inquiry_ref = #{inquiryRef} LIMIT 1")
    Optional<LeadInquiry> findByInquiryRef(@Param("inquiryRef") String inquiryRef);

    @Select("SELECT * FROM lead_inquiries ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<LeadInquiry> findAll(@Param("limit") int limit, @Param("offset") int offset);

    @Update("""
        UPDATE lead_inquiries
        SET email_status = #{emailStatus},
            whatsapp_status = #{whatsappStatus},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    int updateDeliveryStatuses(@Param("id") Long id,
                               @Param("emailStatus") String emailStatus,
                               @Param("whatsappStatus") String whatsappStatus);
}
