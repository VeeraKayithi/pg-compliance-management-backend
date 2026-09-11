package com.pgcompliance.dto;

import com.pgcompliance.constant.AnnouncementPriority;
import com.pgcompliance.constant.AnnouncementStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnnouncementResponseDto {
    private Long announcementId;
    private String title;
    private String message;
    private AnnouncementPriority priority;
    private Boolean sendInApp;
    private Boolean sendEmail;
    private String deepLink;
    private String createdByUsername;
    private AnnouncementStatus status;
    private Integer targetTenantCount;
    private Integer sentCount;
    private Integer failedCount;
    private Integer skippedCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
