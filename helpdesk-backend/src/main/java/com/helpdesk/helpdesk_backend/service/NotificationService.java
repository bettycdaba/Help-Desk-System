package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.NotificationDTO;
import java.util.List;

public interface NotificationService {

    List<NotificationDTO> getUserNotifications(Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    void createNotification(Long userId, Long ticketId,
                            String message, String type);

    void clearAllNotifications(Long userId);
}