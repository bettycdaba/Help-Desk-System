package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
public class TicketHistoryController {

    private final TicketHistoryService historyService;

    @GetMapping("/assignments")
    public ResponseEntity<List<TicketAssignmentHistoryResponseDTO>> getAssignmentHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(historyService.getAssignmentHistory(ticketId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<TicketStatusHistoryResponseDTO>> getStatusHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(historyService.getStatusHistory(ticketId));
    }
}