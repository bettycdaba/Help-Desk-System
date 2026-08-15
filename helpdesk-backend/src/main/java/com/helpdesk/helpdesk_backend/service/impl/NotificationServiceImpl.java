package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.NotificationDTO;
import com.helpdesk.helpdesk_backend.entity.Notification;
import com.helpdesk.helpdesk_backend.entity.Ticket;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.repository.NotificationRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(
            Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(20)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository
            .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                userId)
            .forEach(n -> {
                n.setIsRead(true);
                notificationRepository.save(n);
            });
    }

    @Override
    @Transactional
    public void createNotification(Long userId,
            Long ticketId, String message, String type) {

        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) return;

        Ticket ticket = null;
        if (ticketId != null) {
            ticket = ticketRepository
                    .findById(ticketId).orElse(null);
        }

        Notification notification = Notification.builder()
                .user(user)
                .ticket(ticket)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .ticketId(n.getTicket() != null
                        ? n.getTicket().getId() : null)
                .ticketNumber(n.getTicket() != null
                        ? n.getTicket().getTicketNumber()
                        : null)
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    @Override
@Transactional
public void clearAllNotifications(Long userId) {
    notificationRepository.deleteByUserId(userId);
}
}