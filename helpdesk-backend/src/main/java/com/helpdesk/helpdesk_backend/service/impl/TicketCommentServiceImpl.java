package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketCommentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCommentResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Ticket;
import com.helpdesk.helpdesk_backend.entity.TicketComment;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.TicketCommentRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.EmailService;
import com.helpdesk.helpdesk_backend.service.NotificationService;
import com.helpdesk.helpdesk_backend.service.TicketCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCommentServiceImpl implements TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public TicketCommentResponseDTO addComment(Long ticketId,
                                                TicketCommentRequestDTO request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with id: " + ticketId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        TicketComment comment = TicketComment.builder()
                .comment(request.getComment())
                .commentedAt(LocalDateTime.now())
                .ticket(ticket)
                .user(user)
                .build();

        TicketComment saved = commentRepository.save(comment);

        // =============================================
        // NOTIFY TICKET CREATOR (if not the commenter)
        // =============================================
        if (ticket.getCreatedBy() != null
                && !ticket.getCreatedBy().getId().equals(request.getUserId())) {

            // Send email
            emailService.sendCommentAddedEmail(
                    ticket.getCreatedBy().getEmail(),
                    ticket.getCreatedBy().getFirstName()
                            + " " + ticket.getCreatedBy().getLastName(),
                    ticket.getTicketNumber(),
                    ticket.getSubject(),
                    user.getFirstName() + " " + user.getLastName(),
                    request.getComment()
            );

            // Send notification
            notificationService.createNotification(
                    ticket.getCreatedBy().getId(),
                    ticket.getId(),
                    user.getFirstName() + " " + user.getLastName()
                            + " commented on ticket "
                            + ticket.getTicketNumber() + ".",
                    "comment"
            );
        }

        // =============================================
        // NOTIFY ASSIGNEE (if exists, not commenter,
        // and not the creator already notified)
        // =============================================
        if (ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(request.getUserId())
                && (ticket.getCreatedBy() == null
                        || !ticket.getAssignedTo().getId()
                                .equals(ticket.getCreatedBy().getId()))) {

            // Send email
            emailService.sendCommentAddedEmail(
                    ticket.getAssignedTo().getEmail(),
                    ticket.getAssignedTo().getFirstName()
                            + " " + ticket.getAssignedTo().getLastName(),
                    ticket.getTicketNumber(),
                    ticket.getSubject(),
                    user.getFirstName() + " " + user.getLastName(),
                    request.getComment()
            );

            // Send notification
            notificationService.createNotification(
                    ticket.getAssignedTo().getId(),
                    ticket.getId(),
                    user.getFirstName() + " " + user.getLastName()
                            + " commented on ticket "
                            + ticket.getTicketNumber() + ".",
                    "comment"
            );
        }

        return mapToResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TicketCommentResponseDTO> getCommentsByTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException(
                    "Ticket not found with id: " + ticketId);
        }
        return commentRepository.findByTicketIdOrderByCommentedAtAsc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found with id: " + commentId));
        commentRepository.delete(comment);
    }

    private TicketCommentResponseDTO mapToResponse(TicketComment comment) {
        return TicketCommentResponseDTO.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .commentedAt(comment.getCommentedAt())
                .ticketId(comment.getTicket().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName()
                        + " " + comment.getUser().getLastName())
                .build();
    }
}