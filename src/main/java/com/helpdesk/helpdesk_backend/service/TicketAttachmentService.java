package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketAttachmentResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketAttachmentService {

    TicketAttachmentResponseDTO uploadAttachment(Long ticketId, Long uploadedById, MultipartFile file);

    List<TicketAttachmentResponseDTO> getAttachmentsByTicket(Long ticketId);

    void deleteAttachment(Long attachmentId);
}