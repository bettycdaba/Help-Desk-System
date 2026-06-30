package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;

import java.util.List;

public interface TicketStatusHistoryService {

    List<TicketStatusHistoryResponseDTO> getHistoryByTicket(Long ticketId);
}