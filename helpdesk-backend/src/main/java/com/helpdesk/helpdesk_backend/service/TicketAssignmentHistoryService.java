package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;

import java.util.List;

public interface TicketAssignmentHistoryService {

    List<TicketAssignmentHistoryResponseDTO> getHistoryByTicket(Long ticketId);
}