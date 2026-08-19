package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRejectionRequestDTO {

    @NotNull
    private Long rejectedById;

    @NotBlank
    @Size(max = 2000)
    private String reason;
}