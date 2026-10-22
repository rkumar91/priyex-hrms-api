package com.priyex.hrms.common.whatsapp.mapper;

import com.priyex.hrms.common.whatsapp.model.WhatsAppDeliveryLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface WhatsAppLogMapper {

    @Insert("""
        INSERT INTO whatsapp_delivery_logs (
            inquiry_ref, recipient_phone, template_name, message_body,
            provider, message_id, status, error_message, sent_at, created_at
        ) VALUES (
            #{inquiryRef}, #{recipientPhone}, #{templateName}, #{messageBody},
            #{provider}, #{messageId}, #{status}, #{errorMessage}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WhatsAppDeliveryLog log);
}
