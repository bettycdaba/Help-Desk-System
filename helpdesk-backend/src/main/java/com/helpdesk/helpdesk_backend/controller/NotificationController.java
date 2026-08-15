package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.NotificationDTO;
import com.helpdesk.helpdesk_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>>
            getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(
            notificationService
                .getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>>
            getUnreadCount(@PathVariable Long userId) {
        long count = notificationService
                .getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}")
public ResponseEntity<Void> clearAllNotifications(@PathVariable Long userId) {
    notificationService.clearAllNotifications(userId);
    return ResponseEntity.noContent().build();
}
}