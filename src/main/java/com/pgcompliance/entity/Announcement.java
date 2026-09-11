package com.pgcompliance.entity;

import com.pgcompliance.constant.AnnouncementPriority;
import com.pgcompliance.constant.AnnouncementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long announcementId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementPriority priority;

    @Column(nullable = false)
    private Boolean sendInApp;

    @Column(nullable = false)
    private Boolean sendEmail;

    @Column(name = "deep_link", length = 300)
    private String deepLink;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnnouncementStatus status;

    @Column(nullable = false)
    private Integer targetTenantCount;

    @Column(nullable = false)
    private Integer sentCount;

    @Column(nullable = false)
    private Integer failedCount;

    @Column(nullable = false)
    private Integer skippedCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (targetTenantCount == null) targetTenantCount = 0;
        if (sentCount == null) sentCount = 0;
        if (failedCount == null) failedCount = 0;
        if (skippedCount == null) skippedCount = 0;
    }
}
