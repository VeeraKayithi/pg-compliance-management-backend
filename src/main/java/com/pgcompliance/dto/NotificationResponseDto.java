package com.pgcompliance.dto;

import com.pgcompliance.constant.NotificationDeliveryStatus;
import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDto {

    private Long notificationId;
    private NotificationEventType eventType;
    private NotificationSeverity severity;
    private String sourceModule;
    private String title;
    private String message;
    private String deepLink;
    private String sourceEntityType;
    private Long sourceEntityId;
    private NotificationDeliveryStatus deliveryStatus;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime dismissedAt;
    private Boolean read;
    private Boolean dismissed;
}
