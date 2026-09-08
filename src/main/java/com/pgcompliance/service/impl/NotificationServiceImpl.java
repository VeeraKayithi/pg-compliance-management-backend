package com.pgcompliance.service.impl;

import com.pgcompliance.constant.NotificationDeliveryStatus;
import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import com.pgcompliance.dto.NotificationResponseDto;
import com.pgcompliance.dto.UnreadNotificationCountDto;
import com.pgcompliance.entity.Notification;
import com.pgcompliance.entity.User;
import com.pgcompliance.exception.ResourceNotFoundException;
import com.pgcompliance.repository.NotificationRepository;
import com.pgcompliance.repository.UserRepository;
import com.pgcompliance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
    implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public NotificationResponseDto createNotification(
      Long recipientUserId,
      NotificationEventType eventType,
      NotificationSeverity severity,
      String sourceModule,
      String title,
      String message,
      String deepLink,
      String sourceEntityType,
      Long sourceEntityId,
      String deduplicationKey) {

    User recipient = userRepository
        .findById(recipientUserId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Notification recipient not found with id: "
                + recipientUserId));

    if (!Boolean.TRUE.equals(recipient.getActive())) {
      throw new IllegalStateException(
          "Notification recipient account is inactive");
    }

    if (deduplicationKey != null
        && !deduplicationKey.isBlank()) {

      var duplicate = notificationRepository
          .findFirstByRecipientUserIdAndDeduplicationKeyAndDismissedAtIsNullOrderByCreatedAtDesc(
              recipientUserId,
              deduplicationKey.trim());

      if (duplicate.isPresent()) {
        return mapToResponse(duplicate.get());
      }
    }

    LocalDateTime now = LocalDateTime.now();

    Notification notification = Notification.builder()
        .recipient(recipient)
        .eventType(eventType)
        .severity(severity)
        .sourceModule(sourceModule)
        .title(title)
        .message(message)
        .deepLink(deepLink)
        .sourceEntityType(sourceEntityType)
        .sourceEntityId(sourceEntityId)
        .deduplicationKey(
            deduplicationKey == null
                ? null
                : deduplicationKey.trim())
        .deliveryStatus(
            NotificationDeliveryStatus.DELIVERED)
        .createdAt(now)
        .deliveredAt(now)
        .build();

    return mapToResponse(
        notificationRepository.save(notification));
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResponseDto> getMyNotifications(
      String username) {
    User user = getUser(username);

    return notificationRepository
        .findByRecipientUserIdAndDismissedAtIsNullOrderByCreatedAtDesc(
            user.getUserId())
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResponseDto> getMyUnreadNotifications(
      String username) {
    User user = getUser(username);

    return notificationRepository
        .findByRecipientUserIdAndReadAtIsNullAndDismissedAtIsNullOrderByCreatedAtDesc(
            user.getUserId())
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResponseDto> getMyDismissedNotifications(
      String username) {
    User user = getUser(username);

    return notificationRepository
        .findByRecipientUserIdAndDismissedAtIsNotNullOrderByDismissedAtDesc(
            user.getUserId())
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UnreadNotificationCountDto getMyUnreadCount(
      String username) {
    User user = getUser(username);

    long count = notificationRepository
        .countByRecipientUserIdAndReadAtIsNullAndDismissedAtIsNull(
            user.getUserId());

    return new UnreadNotificationCountDto(count);
  }

  @Override
  @Transactional
  public NotificationResponseDto markAsRead(
      Long notificationId,
      String username) {
    User user = getUser(username);
    Notification notification = getOwnedNotification(
        notificationId,
        user.getUserId());

    if (notification.getReadAt() == null) {
      notification.setReadAt(LocalDateTime.now());
    }

    return mapToResponse(
        notificationRepository.save(notification));
  }

  @Override
  @Transactional
  public NotificationResponseDto dismiss(
      Long notificationId,
      String username) {
    User user = getUser(username);
    Notification notification = getOwnedNotification(
        notificationId,
        user.getUserId());

    LocalDateTime now = LocalDateTime.now();

    if (notification.getReadAt() == null) {
      notification.setReadAt(now);
    }

    if (notification.getDismissedAt() == null) {
      notification.setDismissedAt(now);
    }

    return mapToResponse(
        notificationRepository.save(notification));
  }

  @Override
  @Transactional
  public int markAllAsRead(
      String username) {
    User user = getUser(username);

    return notificationRepository.markAllUnreadAsRead(
        user.getUserId(),
        LocalDateTime.now());
  }

  private User getUser(
      String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User account not found"));
  }

  private Notification getOwnedNotification(
      Long notificationId,
      Long recipientUserId) {
    return notificationRepository
        .findByNotificationIdAndRecipientUserId(
            notificationId,
            recipientUserId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Notification not found"));
  }

  private NotificationResponseDto mapToResponse(
      Notification notification) {
    return NotificationResponseDto.builder()
        .notificationId(
            notification.getNotificationId())
        .eventType(notification.getEventType())
        .severity(notification.getSeverity())
        .sourceModule(
            notification.getSourceModule())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .deepLink(notification.getDeepLink())
        .sourceEntityType(
            notification.getSourceEntityType())
        .sourceEntityId(
            notification.getSourceEntityId())
        .deliveryStatus(
            notification.getDeliveryStatus())
        .createdAt(notification.getCreatedAt())
        .deliveredAt(
            notification.getDeliveredAt())
        .readAt(notification.getReadAt())
        .dismissedAt(
            notification.getDismissedAt())
        .read(notification.getReadAt() != null)
        .dismissed(
            notification.getDismissedAt() != null)
        .build();
  }
}
