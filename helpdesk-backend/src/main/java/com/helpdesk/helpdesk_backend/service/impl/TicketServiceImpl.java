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
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationService notificationService;


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

        /*
         * New tickets always start as OPEN.
         *
         * Assignment is handled separately by the Supervisor/Admin.
         */
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
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET TICKETS BY PRIORITY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getTicketsByPriority(
            TicketPriority priority) {

        return ticketRepository.findByPriority(priority)
                .stream()
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
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // UPDATE TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO updateTicket(
            Long id,
            TicketRequestDTO request) {

        Ticket ticket = findTicketById(id);

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

            TicketCategory category =
                    categoryRepository.findById(request.getCategoryId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Category not found"));

            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(
                ticketRepository.save(ticket)
        );
    }


    // =========================================================
    // UPDATE TICKET DETAILS
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO updateTicketDetails(
            Long id,
            TicketUpdateDTO request) {

        Ticket ticket = findTicketById(id);

        validateReopenedTicketFields(
                ticket,
                request.getSubject(),
                request.getCategoryId()
        );

        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());

        if (request.getCategoryId() != null) {

            TicketCategory category =
                    categoryRepository.findById(request.getCategoryId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Category not found"));

            ticket.setCategory(category);
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(
                ticketRepository.save(ticket)
        );
    }


    // =========================================================
    // ASSIGN / REASSIGN TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO assignTicket(
            Long id,
            TicketAssignRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User newAssignee =
                userRepository.findById(request.getNewAssigneeId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        if (!newAssignee.getActive()) {
            throw new BadRequestException(
                    "Cannot assign to inactive user");
        }

        User assignedBy =
                userRepository.findById(request.getAssignedById())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

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

        /*
         * If the ticket was previously assigned,
         * this is a reassignment.
         *
         * Otherwise the creator is used as the old reference
         * for the initial assignment history.
         */
        User oldAssignee =
                ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo()
                        : ticket.getCreatedBy();

        ticket.setAssignedTo(newAssignee);

        /*
         * Assignment automatically changes the status
         * to ASSIGNED.
         */
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);

        // WebSocket notification
        webSocketNotificationService.notifyTicketUpdate(
                "ASSIGNED",
                mapToResponse(saved)
        );

        // Assignment history
        TicketAssignmentHistory history =
                TicketAssignmentHistory.builder()
                        .ticket(saved)
                        .oldAssignee(oldAssignee)
                        .newAssignee(newAssignee)
                        .assignedBy(assignedBy)
                        .assignedAt(LocalDateTime.now())
                        .build();

        assignmentHistoryRepository.save(history);

        // Notify assigned Support Officer
        notificationService.createNotification(
                newAssignee.getId(),
                saved.getId(),
                "Ticket " + saved.getTicketNumber()
                        + " has been assigned to you by "
                        + assignedBy.getFirstName()
                        + " "
                        + assignedBy.getLastName()
                        + ".",
                "assign"
        );

        // Email assigned Support Officer
        emailService.sendTicketAssignedEmail(
                newAssignee.getEmail(),
                newAssignee.getFirstName()
                        + " "
                        + newAssignee.getLastName(),
                saved.getTicketNumber(),
                saved.getSubject(),
                assignedBy.getFirstName()
                        + " "
                        + assignedBy.getLastName()
        );

        return mapToResponse(saved);
    }


    // =========================================================
    // REJECT TICKET
    // =========================================================

    @Override
    @Transactional
    public TicketResponseDTO rejectTicket(
            Long id,
            TicketRejectionRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User rejectedBy =
                userRepository.findById(request.getRejectedById())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        /*
         * Only Support Officers can reject tickets.
         */
        boolean isSupportOfficer = rejectedBy.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPPORT_OFFICER".equals(role.getName()));

        if (!isSupportOfficer) {
            throw new BadRequestException(
                    "Only the assigned support officer can reject a ticket."
            );
        }

        /*
         * The Support Officer must be the current assignee.
         */
        if (ticket.getAssignedTo() == null
                || !Objects.equals(
                        ticket.getAssignedTo().getId(),
                        rejectedBy.getId())) {

            throw new BadRequestException(
                    "You can only reject a ticket assigned to you."
            );
        }

        /*
         * A Support Officer can reject:
         *
         * 1. A newly assigned ticket
         * 2. A reopened ticket
         */
        if (ticket.getStatus() != TicketStatus.ASSIGNED
                && ticket.getStatus() != TicketStatus.REOPENED) {

            throw new BadRequestException(
                    "This ticket cannot be rejected in its current status."
            );
        }

        User oldAssignee = ticket.getAssignedTo();

        /*
         * Remove the Support Officer.
         */
        ticket.setAssignedTo(null);

        /*
         * Move the ticket to the Supervisor's
         * UNASSIGNED queue.
         */
        ticket.setStatus(TicketStatus.UNASSIGNED);

        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);


        // -----------------------------------------------------
        // Save rejection in assignment history
        // -----------------------------------------------------

        TicketAssignmentHistory history =
                TicketAssignmentHistory.builder()
                        .ticket(saved)
                        .oldAssignee(oldAssignee)
                        .newAssignee(null)
                        .assignedBy(rejectedBy)
                        .assignedAt(LocalDateTime.now())
                        .rejectionReason(request.getReason())
                        .build();

        assignmentHistoryRepository.save(history);


        // -----------------------------------------------------
        // Notify supervisors
        // -----------------------------------------------------

        List<User> supervisors =
                userRepository.findAll()
                        .stream()
                        .filter(User::getActive)
                        .filter(user ->
                                user.getRoles()
                                        .stream()
                                        .anyMatch(role ->
                                                "SUPERVISOR"
                                                        .equals(role.getName())))
                        .collect(Collectors.toList());

        for (User supervisor : supervisors) {

            notificationService.createNotification(
                    supervisor.getId(),
                    saved.getId(),
                    "Ticket "
                            + saved.getTicketNumber()
                            + " was rejected by "
                            + rejectedBy.getFirstName()
                            + " "
                            + rejectedBy.getLastName()
                            + ". Reason: "
                            + request.getReason(),
                    "rejection"
            );
        }


        // -----------------------------------------------------
        // WebSocket notification
        // -----------------------------------------------------

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
    public TicketResponseDTO updateStatus(
            Long id,
            TicketStatusUpdateRequestDTO request) {

        Ticket ticket = findTicketById(id);

        User changedBy =
                userRepository.findById(request.getChangedById())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        validateStatusChange(
                ticket,
                changedBy,
                request.getNewStatus()
        );

        String oldStatus = ticket.getStatus().name();

        /*
         * Change status only once.
         */
        ticket.setStatus(request.getNewStatus());
        ticket.setUpdatedAt(LocalDateTime.now());

        /*
         * Set resolvedAt when the ticket is resolved or closed.
         */
        if (request.getNewStatus() == TicketStatus.RESOLVED
                || request.getNewStatus() == TicketStatus.CLOSED) {

            ticket.setResolvedAt(LocalDateTime.now());
        }

        Ticket saved = ticketRepository.save(ticket);


        // -----------------------------------------------------
        // Save status history
        // -----------------------------------------------------

        TicketStatusHistory history =
                TicketStatusHistory.builder()
                        .ticket(saved)
                        .oldStatus(oldStatus)
                        .newStatus(request.getNewStatus().name())
                        .changedBy(changedBy)
                        .changedAt(LocalDateTime.now())
                        .build();

        statusHistoryRepository.save(history);


        // -----------------------------------------------------
        // Notify ticket creator
        // -----------------------------------------------------

        if (saved.getCreatedBy() != null) {

            if (request.getNewStatus()
                    == TicketStatus.RESOLVED) {

                notificationService.createNotification(
                        saved.getCreatedBy().getId(),
                        saved.getId(),
                        "Your ticket "
                                + saved.getTicketNumber()
                                + " has been resolved.",
                        "resolved"
                );

            } else if (request.getNewStatus()
                    == TicketStatus.REOPENED) {

                /*
                 * Reopened tickets are returned to the
                 * previous Support Officer.
                 *
                 * The assignment itself is handled elsewhere
                 * in the reopening workflow.
                 */
                if (saved.getAssignedTo() != null) {

                    notificationService.createNotification(
                            saved.getAssignedTo().getId(),
                            saved.getId(),
                            "Ticket "
                                    + saved.getTicketNumber()
                                    + " has been reopened.",
                            "status"
                    );
                }

            } else {

                notificationService.createNotification(
                        saved.getCreatedBy().getId(),
                        saved.getId(),
                        "Your ticket "
                                + saved.getTicketNumber()
                                + " status changed: "
                                + oldStatus
                                + " → "
                                + request.getNewStatus().name()
                                + ".",
                        "status"
                );
            }
        }


        // -----------------------------------------------------
        // WebSocket notification
        // -----------------------------------------------------

        webSocketNotificationService.notifyTicketUpdate(
                "STATUS_CHANGED",
                mapToResponse(saved)
        );


        // -----------------------------------------------------
        // Email notification
        // -----------------------------------------------------

        emailService.sendStatusChangedEmail(
                saved.getCreatedBy() != null
                        ? saved.getCreatedBy().getEmail()
                        : "",

                saved.getCreatedBy() != null
                        ? saved.getCreatedBy().getFirstName()
                                + " "
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
    // DELETE TICKET
    // =========================================================

    @Override
    @Transactional
    public void deleteTicket(Long id) {

        ticketRepository.delete(
                findTicketById(id)
        );
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
                .anyMatch(role ->
                        "ADMIN".equals(role.getName()));

        boolean isSupervisor = changedBy.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPERVISOR".equals(role.getName()));

        boolean isSupportOfficer = changedBy.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPPORT_OFFICER"
                                .equals(role.getName()));

        boolean isEmployee = changedBy.getRoles()
                .stream()
                .anyMatch(role ->
                        "EMPLOYEE".equals(role.getName()));

        TicketStatus currentStatus = ticket.getStatus();


        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        if (isAdmin) {

            throw new BadRequestException(
                    "Administrators cannot change ticket status manually."
            );
        }


        // -----------------------------------------------------
        // SUPERVISOR
        // -----------------------------------------------------

        if (isSupervisor) {

            throw new BadRequestException(
                    "Supervisors cannot change ticket status."
            );
        }


        // -----------------------------------------------------
        // SUPPORT OFFICER
        // -----------------------------------------------------

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

                    // ASSIGNED → IN_PROGRESS
                    (
                            currentStatus == TicketStatus.ASSIGNED
                                    && newStatus
                                    == TicketStatus.IN_PROGRESS
                    )

                    // IN_PROGRESS → PENDING
                    || (
                            currentStatus
                                    == TicketStatus.IN_PROGRESS
                                    && newStatus
                                    == TicketStatus.PENDING
                    )

                    // IN_PROGRESS → RESOLVED
                    || (
                            currentStatus
                                    == TicketStatus.IN_PROGRESS
                                    && newStatus
                                    == TicketStatus.RESOLVED
                    )

                    // PENDING → IN_PROGRESS
                    || (
                            currentStatus
                                    == TicketStatus.PENDING
                                    && newStatus
                                    == TicketStatus.IN_PROGRESS
                    )

                    // REOPENED → IN_PROGRESS
                    || (
                            currentStatus
                                    == TicketStatus.REOPENED
                                    && newStatus
                                    == TicketStatus.IN_PROGRESS
                    );

            if (!validTransition) {

                throw new BadRequestException(
                        "Invalid ticket status transition."
                );
            }

            return;
        }


        // -----------------------------------------------------
        // EMPLOYEE
        // -----------------------------------------------------

        if (isEmployee) {

            /*
             * Employee confirms a resolved ticket.
             */
            if (currentStatus == TicketStatus.RESOLVED
                    && newStatus == TicketStatus.CLOSED) {

                return;
            }

            /*
             * Employee can reopen their own closed ticket.
             */
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

        if (!Objects.equals(
                ticket.getSubject(),
                requestedSubject)) {

            throw new BadRequestException(
                    "Employees cannot change the subject of a reopened ticket."
            );
        }

        Long currentCategoryId =
                ticket.getCategory() != null
                        ? ticket.getCategory().getId()
                        : null;

        if (!Objects.equals(
                currentCategoryId,
                requestedCategoryId)) {

            throw new BadRequestException(
                    "Employees cannot change the category of a reopened ticket."
            );
        }
    }


    // =========================================================
    // CHECK CURRENT USER
    // =========================================================

    private boolean isCurrentUserEmployee() {

        var authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return false;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return false;
        }

        boolean hasEmployeeRole =
                user.getRoles()
                        .stream()
                        .anyMatch(role ->
                                "EMPLOYEE"
                                        .equals(role.getName()));

        boolean hasAdminRole =
                user.getRoles()
                        .stream()
                        .anyMatch(role ->
                                "ADMIN"
                                        .equals(role.getName()));

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

    private TicketResponseDTO mapToResponse(
            Ticket ticket) {

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

                .createdById(
                        ticket.getCreatedBy() != null
                                ? ticket.getCreatedBy().getId()
                                : null
                )

                .createdByName(
                        ticket.getCreatedBy() != null
                                ? ticket.getCreatedBy().getFirstName()
                                        + " "
                                        + ticket.getCreatedBy().getLastName()
                                : null
                )

                .assignedToId(
                        ticket.getAssignedTo() != null
                                ? ticket.getAssignedTo().getId()
                                : null
                )

                .assignedToName(
                        ticket.getAssignedTo() != null
                                ? ticket.getAssignedTo().getFirstName()
                                        + " "
                                        + ticket.getAssignedTo().getLastName()
                                : null
                )

                .categoryId(
                        ticket.getCategory() != null
                                ? ticket.getCategory().getId()
                                : null
                )

                .categoryName(
                        ticket.getCategory() != null
                                ? ticket.getCategory().getName()
                                : null
                )

                .build();
    }
}