package com.priyex.hrms.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAnnouncementRequest {
    @NotBlank(message = "Announcement title is required")
    private String title;

    @NotBlank(message = "Announcement body is required")
    private String body;

    private String priority = "NORMAL"; // LOW, NORMAL, HIGH, URGENT
    private String targetAudience = "ALL";
    private Boolean requiresAck = false;
}
