package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketCommentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCommentResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService commentService;

    @PostMapping
    public ResponseEntity<TicketCommentResponseDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketCommentRequestDTO request) {
        return new ResponseEntity<>(
                commentService.addComment(ticketId, request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketCommentResponseDTO>> getComments(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}