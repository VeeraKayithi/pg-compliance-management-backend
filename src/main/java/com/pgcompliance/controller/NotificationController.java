package com.pgcompliance.controller;

import com.pgcompliance.dto.NotificationResponseDto;
import com.pgcompliance.dto.UnreadNotificationCountDto;
import com.pgcompliance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("/me")
  public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.getMyNotifications(
            authentication.getName()));
  }

  @GetMapping("/me/unread")
  public ResponseEntity<List<NotificationResponseDto>> getMyUnreadNotifications(
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.getMyUnreadNotifications(
            authentication.getName()));
  }

  @GetMapping("/me/dismissed")
  public ResponseEntity<List<NotificationResponseDto>> getMyDismissedNotifications(
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.getMyDismissedNotifications(
            authentication.getName()));
  }

  @GetMapping("/me/unread-count")
  public ResponseEntity<UnreadNotificationCountDto> getMyUnreadCount(
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.getMyUnreadCount(
            authentication.getName()));
  }

  @PutMapping("/{notificationId}/read")
  public ResponseEntity<NotificationResponseDto> markAsRead(
      @PathVariable Long notificationId,
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.markAsRead(
            notificationId,
            authentication.getName()));
  }

  @PutMapping("/{notificationId}/dismiss")
  public ResponseEntity<NotificationResponseDto> dismiss(
      @PathVariable Long notificationId,
      Authentication authentication) {

    return ResponseEntity.ok(
        notificationService.dismiss(
            notificationId,
            authentication.getName()));
  }

  @PutMapping("/me/read-all")
  public ResponseEntity<Map<String, Integer>> markAllAsRead(
      Authentication authentication) {

    int updatedCount = notificationService.markAllAsRead(
        authentication.getName());

    return ResponseEntity.ok(
        Map.of(
            "updatedCount",
            updatedCount));
  }
}