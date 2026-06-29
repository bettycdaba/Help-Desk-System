package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketStatus;

import java.util.List;

public interface TicketService {

    TicketResponseDTO createTicket(TicketRequestDTO dto);

    List<TicketResponseDTO> getAllTickets();

    TicketResponseDTO getTicketById(Long id);

    TicketResponseDTO getTicketByTicketNumber(String ticketNumber);

    List<TicketResponseDTO> getTicketsByCreatedBy(Long userId);

    List<TicketResponseDTO> getTicketsByAssignedTo(Long userId);

    List<TicketResponseDTO> getTicketsByStatus(TicketStatus status);

    TicketResponseDTO updateTicket(Long id, TicketRequestDTO dto);

    TicketResponseDTO assignTicket(Long ticketId, Long newAssigneeId, Long assignedById);

    TicketResponseDTO updateStatus(Long ticketId, TicketStatus newStatus, Long changedById);

    void deleteTicket(Long id);
}