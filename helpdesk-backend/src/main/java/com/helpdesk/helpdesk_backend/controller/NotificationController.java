package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.NotificationDTO;
import com.helpdesk.helpdesk_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.helpdesk.helpdesk_backend.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == principal.id or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<NotificationDTO>>
            getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(
            notificationService
                .getUserNotifications(userId));
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("#userId == principal.id or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Long>>
            getUnreadCount(@PathVariable Long userId) {
        long count = notificationService
                .getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        notificationService.markAsRead(id, user.getId(), isAdmin);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("#userId == principal.id or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("#userId == principal.id or hasAuthority('ROLE_ADMIN')")
public ResponseEntity<Void> clearAllNotifications(@PathVariable Long userId) {
    notificationService.clearAllNotifications(userId);
    return ResponseEntity.noContent().build();
}
}
