package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;

import java.util.List;

public interface TicketHistoryService {

    List<TicketAssignmentHistoryResponseDTO> getAssignmentHistory(Long ticketId);

    List<TicketStatusHistoryResponseDTO> getStatusHistory(Long ticketId);
}