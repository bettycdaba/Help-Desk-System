package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketStatusUpdateRequestDTO {

    @NotNull(message = "New status is required")
    private TicketStatus newStatus;

    @NotNull(message = "Changed by user is required")
    private Long changedById;
}