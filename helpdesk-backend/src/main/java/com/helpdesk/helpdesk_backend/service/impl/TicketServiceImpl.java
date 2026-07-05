package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.*;
import com.helpdesk.helpdesk_backend.entity.*;
import com.helpdesk.helpdesk_backend.entity.enums.TicketPriority;
import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.*;
import com.helpdesk.helpdesk_backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketAssignmentHistoryRepository assignmentHistoryRepository;
    private final TicketStatusHistoryRepository statusHistoryRepository;

    @Override
    @Transactional
    public TicketResponseDTO createTicket(TicketRequestDTO request) {

        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getCreatedById()));

        TicketCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssignedToId()));
        }

        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() != null
                        ? request.getPriority() : TicketPriority.MEDIUM)
                .status(assignedTo != null
                        ? TicketStatus.ASSIGNED : TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .category(category)
                .build();

        return mapToResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        return mapToResponse(findTicketById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with number: " + ticketNumber));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByPriority(TicketPriority priority) {
        return ticketRepository.findByPriority(priority)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByCreatedBy(Long userId) {
        return ticketRepository.findByCreatedById(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByAssignedTo(Long userId) {
        return ticketRepository.findByAssignedToId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO request) {
        Ticket ticket = findTicketById(id);

        if (request.getSubject() != null) {
            ticket.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getCategoryId() != null) {
            TicketCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponseDTO assignTicket(Long id, TicketAssignRequestDTO request) {
        Ticket ticket = findTicketById(id);

        User newAssignee = userRepository.findById(request.getNewAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getNewAssigneeId()));

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getAssignedById()));

        User oldAssignee = ticket.getAssignedTo() != null
                ? ticket.getAssignedTo()
                : ticket.getCreatedBy();

        ticket.setAssignedTo(newAssignee);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        TicketAssignmentHistory history = TicketAssignmentHistory.builder()
                .ticket(saved)
                .oldAssignee(oldAssignee)
                .newAssignee(newAssignee)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();

        assignmentHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponseDTO updateStatus(Long id, TicketStatusUpdateRequestDTO request) {
        Ticket ticket = findTicketById(id);

        User changedBy = userRepository.findById(request.getChangedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getChangedById()));

        String oldStatus = ticket.getStatus().name();

        ticket.setStatus(request.getNewStatus());
        ticket.setUpdatedAt(LocalDateTime.now());

        if (request.getNewStatus() == TicketStatus.RESOLVED
                || request.getNewStatus() == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);

        TicketStatusHistory history = TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(request.getNewStatus().name())
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();

        statusHistoryRepository.save(history);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.delete(findTicketById(id));
    }

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with id: " + id));
    }

    private String generateTicketNumber() {
        String uuid = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "TKT-" + uuid;
    }

    private TicketResponseDTO mapToResponse(Ticket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .createdById(ticket.getCreatedBy().getId())
                .createdByName(ticket.getCreatedBy().getFirstName()
                        + " " + ticket.getCreatedBy().getLastName())
                .assignedToId(ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getFirstName()
                        + " " + ticket.getAssignedTo().getLastName() : null)
                .categoryId(ticket.getCategory().getId())
                .categoryName(ticket.getCategory().getName())
                .build();
    }
}