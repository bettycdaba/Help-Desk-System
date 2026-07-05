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
public class TicketCommentResponseDTO {

    private Long id;
    private String comment;
    private LocalDateTime commentedAt;
    private Long ticketId;
    private Long userId;
    private String userName;
}