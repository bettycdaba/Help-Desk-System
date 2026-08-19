package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.*;
import com.helpdesk.helpdesk_backend.entity.enums.TicketPriority;
import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;

import java.util.List;

public interface TicketService {

    TicketResponseDTO createTicket(TicketRequestDTO request);

    List<TicketResponseDTO> getAllTickets();

    TicketResponseDTO getTicketById(Long id);

    TicketResponseDTO getTicketByNumber(String ticketNumber);

    List<TicketResponseDTO> getTicketsByStatus(TicketStatus status);

    List<TicketResponseDTO> getTicketsByPriority(TicketPriority priority);

    List<TicketResponseDTO> getTicketsByCreatedBy(Long userId);

    List<TicketResponseDTO> getTicketsByAssignedTo(Long userId);

    TicketResponseDTO updateTicket(Long id, TicketRequestDTO request);

     TicketResponseDTO updateTicketDetails(Long id, TicketUpdateDTO request); 

    TicketResponseDTO assignTicket(Long id, TicketAssignRequestDTO request);

    TicketResponseDTO updateStatus(Long id, TicketStatusUpdateRequestDTO request);

    void deleteTicket(Long id);

    TicketResponseDTO rejectTicket(
        Long id,
        TicketRejectionRequestDTO request);



}