package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.entity.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRequestDTO {

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private TicketPriority priority;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Creator is required")
    private Long createdById;

    private Long assignedToId;
}