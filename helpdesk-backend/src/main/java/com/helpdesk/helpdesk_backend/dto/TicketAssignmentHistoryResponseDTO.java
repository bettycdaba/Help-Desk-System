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
public class TicketAssignmentHistoryResponseDTO {

    private Long id;
    private Long ticketId;
    private Long oldAssigneeId;
    private String oldAssigneeName;
    private Long newAssigneeId;
    private String newAssigneeName;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;
}