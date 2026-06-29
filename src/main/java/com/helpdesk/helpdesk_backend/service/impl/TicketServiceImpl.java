package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Ticket;
import com.helpdesk.helpdesk_backend.entity.TicketAssignmentHistory;
import com.helpdesk.helpdesk_backend.entity.TicketCategory;
import com.helpdesk.helpdesk_backend.entity.TicketPriority;
import com.helpdesk.helpdesk_backend.entity.TicketStatus;
import com.helpdesk.helpdesk_backend.entity.TicketStatusHistory;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.repository.TicketAssignmentHistoryRepository;
import com.helpdesk.helpdesk_backend.repository.TicketCategoryRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.TicketStatusHistoryRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                              UserRepository userRepository,
                              TicketCategoryRepository categoryRepository,
                              TicketAssignmentHistoryRepository assignmentHistoryRepository,
                              TicketStatusHistoryRepository statusHistoryRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    public TicketResponseDTO createTicket(TicketRequestDTO dto) {
        User createdBy = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + dto.getCreatedById()));

        TicketCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Ticket category not found with id: " + dto.getCategoryId()));

        User assignedTo = null;
        if (dto.getAssignedToId() != null) {
            assignedTo = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new NoSuchElementException("User not found with id: " + dto.getAssignedToId()));
        }

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setSubject(dto.getSubject());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : TicketPriority.MEDIUM);
        ticket.setStatus(assignedTo != null ? TicketStatus.ASSIGNED : TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setCreatedBy(createdBy);
        ticket.setAssignedTo(assignedTo);
        ticket.setCategory(category);

        return toResponseDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketByTicketNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with number: " + ticketNumber));
        return toResponseDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByCreatedBy(Long userId) {
        return ticketRepository.findByCreatedById(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByAssignedTo(Long userId) {
        return ticketRepository.findByAssignedToId(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO dto) {
        Ticket ticket = findEntityById(id);

        if (dto.getSubject() != null) {
            ticket.setSubject(dto.getSubject());
        }
        if (dto.getDescription() != null) {
            ticket.setDescription(dto.getDescription());
        }
        if (dto.getPriority() != null) {
            ticket.setPriority(dto.getPriority());
        }
        if (dto.getCategoryId() != null) {
            TicketCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Ticket category not found with id: " + dto.getCategoryId()));
            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        return toResponseDTO(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponseDTO assignTicket(Long ticketId, Long newAssigneeId, Long assignedById) {
        Ticket ticket = findEntityById(ticketId);

        User newAssignee = userRepository.findById(newAssigneeId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + newAssigneeId));
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + assignedById));

        // old_assignee_id is NOT NULL in the schema. If the ticket was never
        // assigned before, we use the ticket's creator as a placeholder
        // "old assignee" so the very first assignment still has a valid value.
        User oldAssignee = ticket.getAssignedTo() != null ? ticket.getAssignedTo() : ticket.getCreatedBy();

        ticket.setAssignedTo(newAssignee);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketAssignmentHistory history = new TicketAssignmentHistory();
        history.setTicket(savedTicket);
        history.setOldAssignee(oldAssignee);
        history.setNewAssignee(newAssignee);
        history.setAssignedBy(assignedBy);
        history.setAssignedAt(LocalDateTime.now());
        assignmentHistoryRepository.save(history);

        return toResponseDTO(savedTicket);
    }

    @Override
    public TicketResponseDTO updateStatus(Long ticketId, TicketStatus newStatus, Long changedById) {
        Ticket ticket = findEntityById(ticketId);
        User changedBy = userRepository.findById(changedById)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + changedById));

        String oldStatus = ticket.getStatus().name();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicket(savedTicket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus.name());
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return toResponseDTO(savedTicket);
    }

    @Override
    public void deleteTicket(Long id) {
        ticketRepository.delete(findEntityById(id));
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + id));
    }

    private String generateTicketNumber() {
        long timestamp = System.currentTimeMillis() % 1000000;
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TKT-" + timestamp + "-" + randomSuffix;
    }

    private TicketResponseDTO toResponseDTO(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getFirstName() + " " + ticket.getCreatedBy().getLastName(),
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null,
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFirstName() + " " + ticket.getAssignedTo().getLastName() : null,
                ticket.getCategory().getId(),
                ticket.getCategory().getName()
        );
    }
}