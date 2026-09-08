package com.pgcompliance.service;

import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.dto.NotificationResponseDto;
import com.pgcompliance.dto.UnreadNotificationCountDto;

import java.util.List;

public interface NotificationService {

  NotificationResponseDto createNotification(
      Long recipientUserId,
      NotificationEventType eventType,
      NotificationSeverity severity,
      String sourceModule,
      String title,
      String message,
      String deepLink,
      String sourceEntityType,
      Long sourceEntityId,
      String deduplicationKey);

  List<NotificationResponseDto> getMyNotifications(
      String username);

  List<NotificationResponseDto> getMyUnreadNotifications(
      String username);

  List<NotificationResponseDto> getMyDismissedNotifications(
      String username);

  UnreadNotificationCountDto getMyUnreadCount(
      String username);

  NotificationResponseDto markAsRead(
      Long notificationId,
      String username);

  NotificationResponseDto dismiss(
      Long notificationId,
      String username);

  int markAllAsRead(
      String username);
}
