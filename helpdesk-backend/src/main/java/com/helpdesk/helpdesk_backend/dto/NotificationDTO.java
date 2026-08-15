package com.helpdesk.helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String message;
    private String type;
    private Long ticketId;
    private String ticketNumber;
    private Boolean isRead;
    private LocalDateTime createdAt;
}