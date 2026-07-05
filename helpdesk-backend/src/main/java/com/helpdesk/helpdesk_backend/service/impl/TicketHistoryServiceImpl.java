package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketAssignmentHistory;
import com.helpdesk.helpdesk_backend.entity.TicketStatusHistory;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.TicketAssignmentHistoryRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.TicketStatusHistoryRepository;
import com.helpdesk.helpdesk_backend.service.TicketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketHistoryServiceImpl implements TicketHistoryService {

    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketAssignmentHistoryResponseDTO> getAssignmentHistory(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException(
                    "Ticket not found with id: " + ticketId);
        }
        return assignmentHistoryRepository
                .findByTicketIdOrderByAssignedAtDesc(ticketId)
                .stream()
                .map(this::mapAssignmentToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketStatusHistoryResponseDTO> getStatusHistory(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException(
                    "Ticket not found with id: " + ticketId);
        }
        return statusHistoryRepository
                .findByTicketIdOrderByChangedAtDesc(ticketId)
                .stream()
                .map(this::mapStatusToResponse)
                .collect(Collectors.toList());
    }

    private TicketAssignmentHistoryResponseDTO mapAssignmentToResponse(
            TicketAssignmentHistory history) {
        return TicketAssignmentHistoryResponseDTO.builder()
                .id(history.getId())
                .ticketId(history.getTicket().getId())
                .oldAssigneeId(history.getOldAssignee().getId())
                .oldAssigneeName(history.getOldAssignee().getFirstName()
                        + " " + history.getOldAssignee().getLastName())
                .newAssigneeId(history.getNewAssignee().getId())
                .newAssigneeName(history.getNewAssignee().getFirstName()
                        + " " + history.getNewAssignee().getLastName())
                .assignedById(history.getAssignedBy().getId())
                .assignedByName(history.getAssignedBy().getFirstName()
                        + " " + history.getAssignedBy().getLastName())
                .assignedAt(history.getAssignedAt())
                .build();
    }

    private TicketStatusHistoryResponseDTO mapStatusToResponse(
            TicketStatusHistory history) {
        return TicketStatusHistoryResponseDTO.builder()
                .id(history.getId())
                .ticketId(history.getTicket().getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedById(history.getChangedBy().getId())
                .changedByName(history.getChangedBy().getFirstName()
                        + " " + history.getChangedBy().getLastName())
                .changedAt(history.getChangedAt())
                .build();
    }
}