package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketCommentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCommentResponseDTO;

import java.util.List;

public interface TicketCommentService {

    TicketCommentResponseDTO addComment(Long ticketId, TicketCommentRequestDTO request);

    List<TicketCommentResponseDTO> getCommentsByTicket(Long ticketId);

    void deleteComment(Long commentId);
}