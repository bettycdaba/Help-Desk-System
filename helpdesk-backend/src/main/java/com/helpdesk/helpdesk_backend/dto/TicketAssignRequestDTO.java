package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignRequestDTO {

    @NotNull(message = "New assignee is required")
    private Long newAssigneeId;

    @NotNull(message = "Assigned by user is required")
    private Long assignedById;
}