package com.priyex.hrms.notification.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.notification.dto.CreateAnnouncementRequest;
import com.priyex.hrms.notification.mapper.AnnouncementMapper;
import com.priyex.hrms.notification.model.Announcement;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Broadcast Notifications & Announcements", description = "Endpoints for org-wide announcements visible across all users")
public class NotificationController {

    private final AnnouncementMapper announcementMapper;

    @GetMapping("/broadcasts")
    @Operation(summary = "Get active announcements for current organization visible across all users")
    public ResponseEntity<ApiResponse<List<Announcement>>> getBroadcasts(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long userId = (currentUser != null) ? currentUser.getId() : 1L;
        List<Announcement> list = announcementMapper.findAllForUser(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/broadcasts")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Publish a new organization-wide announcement")
    public ResponseEntity<ApiResponse<Announcement>> createBroadcast(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateAnnouncementRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long userId = (currentUser != null) ? currentUser.getId() : 1L;

        Announcement announcement = Announcement.builder()
                .companyId(companyId)
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .priority(request.getPriority() != null ? request.getPriority().toUpperCase() : "NORMAL")
                .targetAudience(request.getTargetAudience() != null ? request.getTargetAudience() : "ALL")
                .isPublished(true)
                .requiresAck(request.getRequiresAck() != null ? request.getRequiresAck() : false)
                .createdBy(userId)
                .build();

        announcementMapper.insert(announcement);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(announcement, "Announcement broadcasted successfully"));
    }

    @PutMapping("/broadcasts/{id}/read")
    @Operation(summary = "Mark an announcement as read / acknowledged by the current user")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long userId = (currentUser != null) ? currentUser.getId() : 1L;
        announcementMapper.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Announcement marked as read"));
    }

    @PutMapping("/broadcasts/read-all")
    @Operation(summary = "Mark all announcements as read for the current user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long userId = (currentUser != null) ? currentUser.getId() : 1L;
        announcementMapper.markAllAsRead(companyId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All announcements marked as read"));
    }

    @DeleteMapping("/broadcasts/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete an announcement (HR/Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteBroadcast(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        announcementMapper.delete(id, companyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Announcement deleted successfully"));
    }
}
