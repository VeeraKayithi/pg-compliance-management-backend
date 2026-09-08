package com.pgcompliance.repository;

import com.pgcompliance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository
    extends JpaRepository<Notification, Long> {

  List<Notification> findByRecipientUserIdAndDismissedAtIsNullOrderByCreatedAtDesc(
      Long recipientUserId);

  List<Notification> findByRecipientUserIdAndReadAtIsNullAndDismissedAtIsNullOrderByCreatedAtDesc(
      Long recipientUserId);

  List<Notification> findByRecipientUserIdAndDismissedAtIsNotNullOrderByDismissedAtDesc(
      Long recipientUserId);

  long countByRecipientUserIdAndReadAtIsNullAndDismissedAtIsNull(
      Long recipientUserId);

  Optional<Notification> findByNotificationIdAndRecipientUserId(
      Long notificationId,
      Long recipientUserId);

  Optional<Notification> findFirstByRecipientUserIdAndDeduplicationKeyAndDismissedAtIsNullOrderByCreatedAtDesc(
      Long recipientUserId,
      String deduplicationKey);

  @Modifying
  @Query("""
      update Notification notification
         set notification.readAt = :readAt
       where notification.recipient.userId = :recipientUserId
         and notification.readAt is null
         and notification.dismissedAt is null
      """)
  int markAllUnreadAsRead(
      @Param("recipientUserId") Long recipientUserId,
      @Param("readAt") LocalDateTime readAt);
}
