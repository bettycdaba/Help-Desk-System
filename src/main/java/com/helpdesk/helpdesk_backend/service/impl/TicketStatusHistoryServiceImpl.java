package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketStatusHistory;
import com.helpdesk.helpdesk_backend.repository.TicketStatusHistoryRepository;
import com.helpdesk.helpdesk_backend.service.TicketStatusHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TicketStatusHistoryServiceImpl implements TicketStatusHistoryService {

    private final TicketStatusHistoryRepository historyRepository;

    public TicketStatusHistoryServiceImpl(TicketStatusHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<TicketStatusHistoryResponseDTO> getHistoryByTicket(Long ticketId) {
        return historyRepository.findByTicketIdOrderByChangedAtDesc(ticketId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private TicketStatusHistoryResponseDTO toResponseDTO(TicketStatusHistory history) {
        return new TicketStatusHistoryResponseDTO(
                history.getId(),
                history.getTicket().getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedBy().getId(),
                history.getChangedBy().getFirstName() + " " + history.getChangedBy().getLastName(),
                history.getChangedAt()
        );
    }
}