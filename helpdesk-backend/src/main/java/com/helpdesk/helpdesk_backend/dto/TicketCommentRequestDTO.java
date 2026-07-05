package com.helpdesk.helpdesk_backend.dto;

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
public class TicketCommentRequestDTO {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 255, message = "Comment cannot exceed 255 characters")
    private String comment;

    @NotNull(message = "User is required")
    private Long userId;
}