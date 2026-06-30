package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketCommentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCommentResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class TicketCommentController {

    private final TicketCommentService commentService;

    public TicketCommentController(TicketCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<TicketCommentResponseDTO> addComment(@PathVariable Long ticketId,
                                                                 @RequestBody TicketCommentRequestDTO dto) {
        dto.setTicketId(ticketId);
        return new ResponseEntity<>(commentService.addComment(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketCommentResponseDTO>> getCommentsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}