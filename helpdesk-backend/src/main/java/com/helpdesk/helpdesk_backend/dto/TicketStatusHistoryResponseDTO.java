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
public class TicketStatusHistoryResponseDTO {

    private Long id;
    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;
}