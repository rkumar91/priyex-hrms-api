package com.priyex.hrms.notification.mapper;

import com.priyex.hrms.notification.model.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AnnouncementMapper {

    @Select("""
        SELECT a.id, a.company_id AS companyId, a.title, a.body, a.priority,
               a.target_audience AS targetAudience, a.target_entity_id AS targetEntityId,
               a.published_at AS publishedAt, a.expires_at AS expiresAt,
               a.is_published AS isPublished, a.requires_ack AS requiresAck,
               a.created_by AS createdBy,
               COALESCE(u.display_name, u.email, 'HR Admin') AS creatorName,
               a.created_at AS createdAt, a.updated_at AS updatedAt,
               CASE WHEN ack.user_id IS NOT NULL THEN TRUE ELSE FALSE END AS isRead,
               ack.acknowledged_at AS acknowledgedAt
        FROM announcements a
        LEFT JOIN users u ON u.id = a.created_by
        LEFT JOIN announcement_acknowledgments ack ON ack.announcement_id = a.id AND ack.user_id = #{userId}
        WHERE a.company_id = #{companyId} AND a.is_published = TRUE
          AND (a.published_at IS NULL OR a.published_at <= NOW())
          AND (a.expires_at IS NULL OR a.expires_at > NOW())
        ORDER BY
            CASE a.priority
                WHEN 'URGENT' THEN 1
                WHEN 'HIGH' THEN 2
                WHEN 'NORMAL' THEN 3
                ELSE 4
            END ASC,
            a.created_at DESC
    """)
    List<Announcement> findAllForUser(@Param("companyId") Long companyId, @Param("userId") Long userId);

    @Select("""
        SELECT a.id, a.company_id AS companyId, a.title, a.body, a.priority,
               a.target_audience AS targetAudience, a.target_entity_id AS targetEntityId,
               a.published_at AS publishedAt, a.expires_at AS expiresAt,
               a.is_published AS isPublished, a.requires_ack AS requiresAck,
               a.created_by AS createdBy,
               a.created_at AS createdAt, a.updated_at AS updatedAt
        FROM announcements a
        WHERE a.id = #{id} AND a.company_id = #{companyId}
    """)
    Optional<Announcement> findById(@Param("id") Long id, @Param("companyId") Long companyId);

    /** Admin view: every announcement (scheduled, live, expired) with read counts. */
    @Select("""
        SELECT a.id, a.company_id AS companyId, a.title, a.body, a.priority,
               a.target_audience AS targetAudience, a.target_entity_id AS targetEntityId,
               a.published_at AS publishedAt, a.expires_at AS expiresAt,
               a.is_published AS isPublished, a.requires_ack AS requiresAck,
               a.created_by AS createdBy,
               COALESCE(u.display_name, u.email, 'HR Admin') AS creatorName,
               a.created_at AS createdAt, a.updated_at AS updatedAt,
               (SELECT COUNT(*) FROM announcement_acknowledgments ack WHERE ack.announcement_id = a.id) AS readCount
        FROM announcements a
        LEFT JOIN users u ON u.id = a.created_by
        WHERE a.company_id = #{companyId}
        ORDER BY COALESCE(a.published_at, a.created_at) DESC
    """)
    List<Announcement> findAllForAdmin(@Param("companyId") Long companyId);

    @Insert("""
        INSERT INTO announcements (
            company_id, title, body, priority, target_audience, target_entity_id,
            published_at, expires_at, is_published, requires_ack, created_by, created_at, updated_at
        ) VALUES (
            #{companyId}, #{title}, #{body}, #{priority}, #{targetAudience}, #{targetEntityId},
            COALESCE(#{publishedAt}, NOW()), #{expiresAt}, TRUE, #{requiresAck}, #{createdBy}, NOW(), NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("""
        UPDATE announcements
        SET title = #{title}, body = #{body}, priority = #{priority},
            target_audience = #{targetAudience}, requires_ack = #{requiresAck},
            published_at = COALESCE(#{publishedAt}, published_at, NOW()),
            expires_at = #{expiresAt},
            updated_at = NOW()
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int update(Announcement announcement);

    /** Immediately take an announcement off-air by closing its window now. */
    @Update("""
        UPDATE announcements
        SET expires_at = NOW(), updated_at = NOW()
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int endNow(@Param("id") Long id, @Param("companyId") Long companyId);

    @Insert("""
        INSERT INTO announcement_acknowledgments (announcement_id, user_id, acknowledged_at)
        VALUES (#{announcementId}, #{userId}, NOW())
        ON CONFLICT (announcement_id, user_id) DO NOTHING
    """)
    int markAsRead(@Param("announcementId") Long announcementId, @Param("userId") Long userId);

    @Insert("""
        INSERT INTO announcement_acknowledgments (announcement_id, user_id, acknowledged_at)
        SELECT a.id, #{userId}, NOW()
        FROM announcements a
        WHERE a.company_id = #{companyId} AND a.is_published = TRUE
          AND (a.published_at IS NULL OR a.published_at <= NOW())
          AND (a.expires_at IS NULL OR a.expires_at > NOW())
        ON CONFLICT (announcement_id, user_id) DO NOTHING
    """)
    int markAllAsRead(@Param("companyId") Long companyId, @Param("userId") Long userId);

    @Delete("""
        DELETE FROM announcements
        WHERE id = #{id} AND company_id = #{companyId}
    """)
    int delete(@Param("id") Long id, @Param("companyId") Long companyId);
}
