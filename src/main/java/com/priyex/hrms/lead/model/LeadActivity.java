package com.priyex.hrms.lead.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One entry in a lead's conversation history: a note, call, stage change or drop.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadActivity {
    private Long id;
    private Long leadId;
    private String activityType; // NOTE, CALL, EMAIL, WHATSAPP, MEETING, STAGE_CHANGE, DROPPED
    private String fromStatus;
    private String toStatus;
    private String note;
    private Instant nextFollowUpAt;
    private String createdBy;
    private Instant createdAt;
}
