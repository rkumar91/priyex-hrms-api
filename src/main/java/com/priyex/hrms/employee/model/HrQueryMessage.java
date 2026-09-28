package com.priyex.hrms.employee.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrQueryMessage {
    private Long id;
    private Long queryId;
    private Long senderUserId;
    private String senderType; // 'EMPLOYEE' or 'HR'
    private String senderName;
    private String messageText;
    private Instant createdAt;
}
