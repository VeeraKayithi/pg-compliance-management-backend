package com.pgcompliance.entity;

import com.pgcompliance.constant.NotificationDeliveryStatus;
import com.pgcompliance.constant.NotificationEventType;
import com.pgcompliance.constant.NotificationSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient_created", columnList = "recipient_user_id, created_at"),
    @Index(name = "idx_notification_recipient_read", columnList = "recipient_user_id, read_at"),
    @Index(name = "idx_notification_deduplication", columnList = "recipient_user_id, deduplication_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recipient_user_id", nullable = false)
  private User recipient;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 60)
  private NotificationEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationSeverity severity;

  @Column(name = "source_module", nullable = false, length = 50)
  private String sourceModule;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 1000)
  private String message;

  @Column(name = "deep_link", length = 300)
  private String deepLink;

  @Column(name = "source_entity_type", length = 50)
  private String sourceEntityType;

  @Column(name = "source_entity_id")
  private Long sourceEntityId;

  @Column(name = "deduplication_key", length = 150)
  private String deduplicationKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_status", nullable = false, length = 20)
  private NotificationDeliveryStatus deliveryStatus;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "dismissed_at")
  private LocalDateTime dismissedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }

    if (deliveryStatus == null) {
      deliveryStatus = NotificationDeliveryStatus.PENDING;
    }
  }
}
