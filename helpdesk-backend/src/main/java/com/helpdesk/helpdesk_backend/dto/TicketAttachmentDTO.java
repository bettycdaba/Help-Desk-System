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
public class TicketAttachmentDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private Long ticketId;
    private Long uploadedById;
    private String uploadedByName;
}