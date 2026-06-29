package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketAssignmentHistory;
import com.helpdesk.helpdesk_backend.repository.TicketAssignmentHistoryRepository;
import com.helpdesk.helpdesk_backend.service.TicketAssignmentHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketAssignmentHistoryServiceImpl implements TicketAssignmentHistoryService {

    private final TicketAssignmentHistoryRepository historyRepository;

    public TicketAssignmentHistoryServiceImpl(TicketAssignmentHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<TicketAssignmentHistoryResponseDTO> getHistoryByTicket(Long ticketId) {
        return historyRepository.findByTicketIdOrderByAssignedAtDesc(ticketId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private TicketAssignmentHistoryResponseDTO toResponseDTO(TicketAssignmentHistory history) {
        return new TicketAssignmentHistoryResponseDTO(
                history.getId(),
                history.getTicket().getId(),
                history.getOldAssignee().getId(),
                history.getOldAssignee().getFirstName() + " " + history.getOldAssignee().getLastName(),
                history.getNewAssignee().getId(),
                history.getNewAssignee().getFirstName() + " " + history.getNewAssignee().getLastName(),
                history.getAssignedBy().getId(),
                history.getAssignedBy().getFirstName() + " " + history.getAssignedBy().getLastName(),
                history.getAssignedAt()
        );
    }
}