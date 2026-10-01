package com.priyex.hrms.common.email;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface EmailLogMapper {

    @Insert("""
        INSERT INTO email_delivery_logs (
            recipient_email, subject, template_code, status, error_message, sent_at, created_at
        ) VALUES (
            #{recipientEmail}, #{subject}, #{templateCode}, #{status}, #{errorMessage}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmailDeliveryLog log);
}
