package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.*;
import com.helpdesk.helpdesk_backend.entity.*;
import com.helpdesk.helpdesk_backend.entity.enums.TicketPriority;
import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;
import com.helpdesk.helpdesk_backend.exception.BadRequestException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.*;
import com.helpdesk.helpdesk_backend.service.EmailService;
import com.helpdesk.helpdesk_backend.service.NotificationService;
import com.helpdesk.helpdesk_backend.service.TicketService;
import com.helpdesk.helpdesk_backend.service.WebSocketNotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    private final TicketCommentRepository commentRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationService notificationService;

    private static final int MAX_ACTIVE_TICKETS_PER_OFFICER = 10;

    // =========================================================
    // CREATE TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO createTicket(TicketRequestDTO request) {

        User createdBy = userRepository.findById(request.getCreatedById())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        TicketCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        User assignedTo = null;

        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("User not found"));
        }

        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber())
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(
                        request.getPriority() != null
                                ? request.getPriority()
                                : TicketPriority.MEDIUM
                )
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .category(category)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        emailService.sendTicketCreatedEmail(
                createdBy.getEmail(),
                createdBy.getFirstName() + " " + createdBy.getLastName(),
                saved.getTicketNumber(),
                saved.getSubject()
        );

        notifySupervisorsAndAdmins(saved, createdBy);

        return mapToResponse(saved);
    }

    // =========================================================
    // GET ALL TICKETS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

        @Override
        @Transactional(readOnly = true)
        public List<TicketResponseDTO> getArchivedTickets() {
                User currentUser = getAuthenticatedUser();
                boolean canViewAll = currentUser.getRoles().stream()
                                .anyMatch(role -> "ADMIN".equals(role.getName())
                                                || "SUPERVISOR".equals(role.getName()));

                List<Ticket> archivedTickets;
                if (canViewAll) {
                        archivedTickets = ticketRepository.findByArchivedTrue();
                } else {
                        archivedTickets = new java.util.ArrayList<>(
                                        ticketRepository.findByCreatedByIdAndArchivedTrue(currentUser.getId()));
                        ticketRepository.findByAssignedToIdAndArchivedTrue(currentUser.getId())
                                        .forEach(ticket -> {
                                                if (archivedTickets.stream().noneMatch(existing ->
                                                                existing.getId().equals(ticket.getId()))) {
                                                        archivedTickets.add(ticket);
                                                }
                                        });
                }

                return archivedTickets.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

    // =========================================================
    // GET TICKET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        return mapToResponse(findTicketById(id));
    }

    // =========================================================
    // GET TICKET BY NUMBER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));
        return mapToResponse(ticket);
    }

    // =========================================================
    // GET TICKETS BY STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET TICKETS BY PRIORITY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByPriority(TicketPriority priority) {
        return ticketRepository.findByPriority(priority)
                .stream()
                .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET TICKETS CREATED BY USER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByCreatedBy(Long userId) {
        return ticketRepository.findByCreatedById(userId)
                .stream()
                .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET TICKETS ASSIGNED TO USER
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByAssignedTo(Long userId) {
        return ticketRepository.findByAssignedToId(userId)
                .stream()
                .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPDATE TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO updateTicket(Long id, TicketRequestDTO request) {

        Ticket ticket = findTicketById(id);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Closed tickets cannot be edited.");
        }

        validateReopenedTicketFields(
                ticket,
                request.getSubject(),
                request.getCategoryId()
        );

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
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Category not found"));
            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(ticketRepository.save(ticket));
    }

    // =========================================================
    // UPDATE TICKET DETAILS
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO updateTicketDetails(Long id, TicketUpdateDTO request) {

        Ticket ticket = findTicketById(id);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Closed tickets cannot be edited.");
        }

        validateReopenedTicketFields(
                ticket,
                request.getSubject(),
                request.getCategoryId()
        );

        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        if (request.getCategoryId() != null) {
            TicketCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Category not found"));
            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(ticketRepository.save(ticket));
    }

    // =========================================================
    // ASSIGN / REASSIGN TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO assignTicket(Long id, TicketAssignRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User newAssignee = userRepository.findById(request.getNewAssigneeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!newAssignee.getActive()) {
            throw new BadRequestException("Cannot assign to inactive user");
        }

        User assignedBy = userRepository.findById(request.getAssignedById())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean canAssign = assignedBy.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPERVISOR".equals(role.getName())
                                || "ADMIN".equals(role.getName())
                );

        if (!canAssign) {
            throw new BadRequestException(
                    "Only supervisors or administrators can assign tickets."
            );
        }

        if (ticket.getAssignedTo() != null
                && Objects.equals(
                        ticket.getAssignedTo().getId(),
                        assignedBy.getId())) {
            throw new BadRequestException(
                    "You cannot reassign a ticket that is already assigned to you. "
                            + "Please reject it first if you cannot handle it."
            );
        }

        User oldAssignee = ticket.getAssignedTo() != null
                ? ticket.getAssignedTo()
                : ticket.getCreatedBy();

        String oldStatus = ticket.getStatus().name();

        ticket.setAssignedTo(newAssignee);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        webSocketNotificationService.notifyTicketUpdate(
                "ASSIGNED",
                mapToResponse(saved)
        );

        TicketAssignmentHistory history = TicketAssignmentHistory.builder()
                .ticket(saved)
                .oldAssignee(oldAssignee)
                .newAssignee(newAssignee)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();

        assignmentHistoryRepository.save(history);

        TicketStatusHistory statusHistory = TicketStatusHistory.builder()
                .ticket(saved)
                .oldStatus(oldStatus)
                .newStatus(TicketStatus.ASSIGNED.name())
                .changedBy(assignedBy)
                .changedAt(LocalDateTime.now())
                .build();

        statusHistoryRepository.save(statusHistory);

        notificationService.createNotification(
                newAssignee.getId(),
                saved.getId(),
                "Ticket " + saved.getTicketNumber()
                        + " has been assigned to you by "
                        + assignedBy.getFirstName() + " "
                        + assignedBy.getLastName() + ".",
                "assign"
        );

        emailService.sendTicketAssignedEmail(
                newAssignee.getEmail(),
                newAssignee.getFirstName() + " " + newAssignee.getLastName(),
                saved.getTicketNumber(),
                saved.getSubject(),
                assignedBy.getFirstName() + " " + assignedBy.getLastName()
        );

        return mapToResponse(saved);
    }

    // =========================================================
    // AUTO ASSIGN TICKET (Least-Busy Support Officer)
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO autoAssignTicket(Long ticketId, Long assignedById) {

        Ticket ticket = findTicketById(ticketId);

        if (ticket.getAssignedTo() != null
                && Objects.equals(
                        ticket.getAssignedTo().getId(),
                        assignedById)) {
            throw new BadRequestException(
                    "This ticket is already assigned to you."
            );
        }

        if (ticket.getStatus() != TicketStatus.OPEN
                && ticket.getStatus() != TicketStatus.UNASSIGNED) {
            throw new BadRequestException(
                    "Only OPEN or UNASSIGNED tickets can be auto-assigned."
            );
        }

        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean canAssign = assignedBy.getRoles().stream()
                .anyMatch(role ->
                        "SUPERVISOR".equals(role.getName())
                                || "ADMIN".equals(role.getName()));

        if (!canAssign) {
            throw new BadRequestException(
                    "Only supervisors or administrators can auto-assign tickets."
            );
        }

        List<User> officers = userRepository.findActiveSupportOfficers();

        if (officers.isEmpty()) {
            throw new BadRequestException(
                    "No active Support Officers are available."
            );
        }

        List<Long> previousRejecterIds =
                assignmentHistoryRepository
                        .findOfficerIdsWhoRejectedTicket(ticketId);

        User leastBusy = null;
        long lowestCount = Long.MAX_VALUE;

        for (User officer : officers) {

            if (previousRejecterIds.contains(officer.getId())) {
                continue;
            }

            long activeCount = ticketRepository
                    .findActiveTicketsByAssignee(officer.getId())
                    .size();

            if (activeCount >= MAX_ACTIVE_TICKETS_PER_OFFICER) {
                continue;
            }

            if (activeCount < lowestCount) {
                lowestCount = activeCount;
                leastBusy = officer;
            }
        }

        if (leastBusy == null) {
            if (previousRejecterIds.size() >= officers.size()) {
                throw new BadRequestException(
                        "All available Support Officers have already rejected "
                                + "this ticket. Please assign manually."
                );
            }
            throw new BadRequestException(
                    "All eligible Support Officers are currently at maximum capacity."
            );
        }

        TicketAssignRequestDTO request = new TicketAssignRequestDTO();
        request.setNewAssigneeId(leastBusy.getId());
        request.setAssignedById(assignedBy.getId());

        return assignTicket(ticketId, request);
    }

    // =========================================================
    // REJECT TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO rejectTicket(Long id, TicketRejectionRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User rejectedBy = userRepository.findById(request.getRejectedById())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean isSupportOfficer = rejectedBy.getRoles()
                .stream()
                .anyMatch(role -> "SUPPORT_OFFICER".equals(role.getName()));

        if (!isSupportOfficer) {
            throw new BadRequestException(
                    "Only the assigned support officer can reject a ticket."
            );
        }

        if (ticket.getAssignedTo() == null
                || !Objects.equals(
                        ticket.getAssignedTo().getId(),
                        rejectedBy.getId())) {
            throw new BadRequestException(
                    "You can only reject a ticket assigned to you."
            );
        }

        if (ticket.getStatus() != TicketStatus.ASSIGNED
                && ticket.getStatus() != TicketStatus.REOPENED) {
            throw new BadRequestException(
                    "This ticket cannot be rejected in its current status."
            );
        }

        User oldAssignee = ticket.getAssignedTo();

        ticket.setAssignedTo(null);
        ticket.setStatus(TicketStatus.UNASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        TicketAssignmentHistory history = TicketAssignmentHistory.builder()
                .ticket(saved)
                .oldAssignee(oldAssignee)
                .newAssignee(null)
                .assignedBy(rejectedBy)
                .assignedAt(LocalDateTime.now())
                .rejectionReason(request.getReason())
                .build();

        assignmentHistoryRepository.save(history);

        List<User> supervisors = userRepository.findAll()
                .stream()
                .filter(User::getActive)
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "SUPERVISOR".equals(role.getName())))
                .collect(Collectors.toList());

        for (User supervisor : supervisors) {
            notificationService.createNotification(
                    supervisor.getId(),
                    saved.getId(),
                    "Ticket " + saved.getTicketNumber()
                            + " was rejected by "
                            + rejectedBy.getFirstName() + " "
                            + rejectedBy.getLastName()
                            + ". Reason: " + request.getReason(),
                    "rejection"
            );
        }

        webSocketNotificationService.notifyTicketUpdate(
                "TICKET_REJECTED",
                mapToResponse(saved)
        );

        return mapToResponse(saved);
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO updateStatus(Long id, TicketStatusUpdateRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User changedBy = userRepository.findById(request.getChangedById())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        validateStatusChange(ticket, changedBy, request.getNewStatus());

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

        if (saved.getCreatedBy() != null) {

            if (request.getNewStatus() == TicketStatus.RESOLVED) {

                notificationService.createNotification(
                        saved.getCreatedBy().getId(),
                        saved.getId(),
                        "Your ticket " + saved.getTicketNumber()
                                + " has been resolved.",
                        "resolved"
                );

            } else if (request.getNewStatus() == TicketStatus.REOPENED) {

                if (saved.getAssignedTo() != null) {
                    notificationService.createNotification(
                            saved.getAssignedTo().getId(),
                            saved.getId(),
                            "Ticket " + saved.getTicketNumber()
                                    + " has been reopened.",
                            "status"
                    );
                }

            } else {

                notificationService.createNotification(
                        saved.getCreatedBy().getId(),
                        saved.getId(),
                        "Your ticket " + saved.getTicketNumber()
                                + " status changed: "
                                + oldStatus + " → "
                                + request.getNewStatus().name() + ".",
                        "status"
                );
            }
        }

        webSocketNotificationService.notifyTicketUpdate(
                "STATUS_CHANGED",
                mapToResponse(saved)
        );

        emailService.sendStatusChangedEmail(
                saved.getCreatedBy() != null
                        ? saved.getCreatedBy().getEmail() : "",
                saved.getCreatedBy() != null
                        ? saved.getCreatedBy().getFirstName() + " "
                                + saved.getCreatedBy().getLastName()
                        : "Unknown",
                saved.getTicketNumber(),
                saved.getSubject(),
                oldStatus,
                request.getNewStatus().name()
        );

        return mapToResponse(saved);
    }

    // =========================================================
        // ARCHIVE TICKET
    // =========================================================

    @Override
    @Transactional
        public TicketResponseDTO archiveTicket(Long id, Long archivedById) {
                Ticket ticket = findTicketById(id);
                User archivedBy = userRepository.findById(archivedById)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                if (Boolean.TRUE.equals(ticket.getArchived())) {
                        return mapToResponse(ticket);
                }

                ticket.setArchived(true);
                ticket.setArchivedAt(LocalDateTime.now());
                ticket.setArchivedBy(archivedBy);
                return mapToResponse(ticketRepository.save(ticket));
    }

        @Override
        @Transactional
        public TicketResponseDTO unarchiveTicket(Long id) {
                Ticket ticket = findTicketById(id);
                ticket.setArchived(false);
                ticket.setArchivedAt(null);
                ticket.setArchivedBy(null);
                return mapToResponse(ticketRepository.save(ticket));
        }

    // =========================================================
    // DELETE TICKET (Only if no activity history)
    // =========================================================

    @Override
    @Transactional
    public void deleteTicket(Long id) {
        Ticket ticket = findTicketById(id);
                User currentUser = getAuthenticatedUser();

                if (ticket.getCreatedBy() == null
                                || !Objects.equals(ticket.getCreatedBy().getId(), currentUser.getId())) {
                        throw new BadRequestException(
                                        "Only the ticket creator can delete this ticket.");
                }
                if (ticket.getAssignedTo() != null) {
                        throw new BadRequestException(
                                        "Assigned tickets cannot be deleted.");
                }

        boolean hasComments = commentRepository.existsByTicketId(id);
        boolean hasAttachments = attachmentRepository.existsByTicketId(id);
        boolean hasStatusHistory = statusHistoryRepository.existsByTicketId(id);
        boolean hasAssignmentHistory = assignmentHistoryRepository.existsByTicketId(id);
        boolean isAssigned = ticket.getAssignedTo() != null;
        boolean hasAdvancedStatus = ticket.getStatus() != TicketStatus.OPEN
                && ticket.getStatus() != TicketStatus.UNASSIGNED;

        if (hasComments || hasAttachments || hasStatusHistory || hasAssignmentHistory || isAssigned || hasAdvancedStatus) {
            throw new BadRequestException(
                "Tickets with activity history cannot be permanently deleted to preserve records and support accountability. Please close or archive the ticket instead.");
        }

        ticketRepository.delete(ticket);
    }

    // =========================================================
    // VALIDATE STATUS CHANGE
    // =========================================================

    private void validateStatusChange(
            Ticket ticket,
            User changedBy,
            TicketStatus newStatus) {

        boolean isAdmin = changedBy.getRoles()
                .stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        boolean isSupervisor = changedBy.getRoles()
                .stream()
                .anyMatch(role -> "SUPERVISOR".equals(role.getName()));

        boolean isSupportOfficer = changedBy.getRoles()
                .stream()
                .anyMatch(role -> "SUPPORT_OFFICER".equals(role.getName()));

        boolean isEmployee = changedBy.getRoles()
                .stream()
                .anyMatch(role -> "EMPLOYEE".equals(role.getName()));

        TicketStatus currentStatus = ticket.getStatus();

        if (isAdmin) {
            throw new BadRequestException(
                    "Administrators cannot change ticket status manually."
            );
        }

        if (isSupervisor) {
            throw new BadRequestException(
                    "Supervisors cannot change ticket status."
            );
        }

        if (isSupportOfficer) {

            if (ticket.getAssignedTo() == null
                    || !Objects.equals(
                            ticket.getAssignedTo().getId(),
                            changedBy.getId())) {
                throw new BadRequestException(
                        "You can only change the status of tickets assigned to you."
                );
            }

            boolean validTransition =
                    (currentStatus == TicketStatus.ASSIGNED
                            && newStatus == TicketStatus.IN_PROGRESS)
                    || (currentStatus == TicketStatus.IN_PROGRESS
                            && newStatus == TicketStatus.PENDING)
                    || (currentStatus == TicketStatus.IN_PROGRESS
                            && newStatus == TicketStatus.RESOLVED)
                    || (currentStatus == TicketStatus.PENDING
                            && newStatus == TicketStatus.IN_PROGRESS)
                    || (currentStatus == TicketStatus.REOPENED
                            && newStatus == TicketStatus.IN_PROGRESS);

            if (!validTransition) {
                throw new BadRequestException("Invalid ticket status transition.");
            }

            return;
        }

        if (isEmployee) {

            if (currentStatus == TicketStatus.RESOLVED
                    && newStatus == TicketStatus.CLOSED) {
                return;
            }

            if (currentStatus == TicketStatus.CLOSED
                    && newStatus == TicketStatus.REOPENED
                    && ticket.getCreatedBy() != null
                    && Objects.equals(
                            ticket.getCreatedBy().getId(),
                            changedBy.getId())) {
                return;
            }

            throw new BadRequestException(
                    "You are not allowed to make this status change."
            );
        }

        throw new BadRequestException(
                "You do not have permission to change ticket status."
        );
    }

    // =========================================================
    // FIND TICKET
    // =========================================================

    private Ticket findTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with id: " + id));
    }

    // =========================================================
    // VALIDATE REOPENED TICKET
    // =========================================================

    private void validateReopenedTicketFields(
            Ticket ticket,
            String requestedSubject,
            Long requestedCategoryId) {

        if (ticket.getStatus() != TicketStatus.REOPENED
                || !isCurrentUserEmployee()) {
            return;
        }

        if (!Objects.equals(ticket.getSubject(), requestedSubject)) {
            throw new BadRequestException(
                    "Employees cannot change the subject of a reopened ticket."
            );
        }

        Long currentCategoryId = ticket.getCategory() != null
                ? ticket.getCategory().getId()
                : null;

        if (!Objects.equals(currentCategoryId, requestedCategoryId)) {
            throw new BadRequestException(
                    "Employees cannot change the category of a reopened ticket."
            );
        }
    }

    // =========================================================
    // CHECK CURRENT USER
    // =========================================================

        private User getAuthenticatedUser() {
                var authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                if (authentication == null
                                || !(authentication.getPrincipal() instanceof User user)) {
                        throw new BadRequestException("Unable to identify the current user.");
                }

                return user;
        }

    private boolean isCurrentUserEmployee() {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return false;
        }

        boolean hasEmployeeRole = user.getRoles()
                .stream()
                .anyMatch(role -> "EMPLOYEE".equals(role.getName()));

        boolean hasAdminRole = user.getRoles()
                .stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        return hasEmployeeRole && !hasAdminRole;
    }

    // =========================================================
    // GENERATE TICKET NUMBER
    // =========================================================

    private String generateTicketNumber() {
        return "TKT-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }

    // =========================================================
    // MAP ENTITY → RESPONSE DTO
    // =========================================================

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
                .createdById(ticket.getCreatedBy() != null
                        ? ticket.getCreatedBy().getId() : null)
                .createdByName(ticket.getCreatedBy() != null
                        ? ticket.getCreatedBy().getFirstName() + " "
                                + ticket.getCreatedBy().getLastName()
                        : null)
                .assignedToId(ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getFirstName() + " "
                                + ticket.getAssignedTo().getLastName()
                        : null)
                .categoryId(ticket.getCategory() != null
                        ? ticket.getCategory().getId() : null)
                .categoryName(ticket.getCategory() != null
                        ? ticket.getCategory().getName() : null)
                .archived(ticket.getArchived())
                .archivedAt(ticket.getArchivedAt())
                .archivedById(ticket.getArchivedBy() != null
                        ? ticket.getArchivedBy().getId() : null)
                .build();
    }

    // =========================================================
    // TEAM WORKLOAD
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TeamWorkloadDTO> getTeamWorkload() {

        List<User> supportOfficers = userRepository.findActiveSupportOfficers();

        return supportOfficers.stream().map(officer -> {

            List<Ticket> officerTickets =
                    ticketRepository.findByAssignedToId(officer.getId());

            officerTickets = officerTickets.stream()
                    .filter(ticket -> !Boolean.TRUE.equals(ticket.getArchived()))
                    .collect(Collectors.toList());

            return TeamWorkloadDTO.builder()
                    .userId(officer.getId())
                    .firstName(officer.getFirstName())
                    .lastName(officer.getLastName())
                    .email(officer.getEmail())
                    .assignedCount(officerTickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.ASSIGNED).count())
                    .inProgressCount(officerTickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count())
                    .pendingCount(officerTickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.PENDING).count())
                    .resolvedCount(officerTickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.RESOLVED).count())
                    .totalCount(officerTickets.size())
                    .build();
        }).collect(Collectors.toList());
    }

    // =========================================================
    // NOTIFY SUPERVISORS AND ADMINS
    // =========================================================

    private void notifySupervisorsAndAdmins(Ticket ticket, User createdBy) {

        List<User> supervisorsAndAdmins = userRepository.findAll()
                .stream()
                .filter(User::getActive)
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role ->
                                "SUPERVISOR".equals(role.getName())
                                        || "ADMIN".equals(role.getName())))
                .collect(Collectors.toList());

        String message = "New ticket " + ticket.getTicketNumber()
                + " created by " + createdBy.getFirstName() + " "
                + createdBy.getLastName() + ": " + ticket.getSubject();

        for (User supervisor : supervisorsAndAdmins) {
            notificationService.createNotification(
                    supervisor.getId(),
                    ticket.getId(),
                    message,
                    "new_ticket"
            );
        }
    }
}