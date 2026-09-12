package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRejectRequestDTO {

    @NotNull(message = "Rejected by user is required")
    private Long rejectedById;

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 500,
        message = "Rejection reason must be between "
            + "10 and 500 characters")
    private String rejectionReason;
}