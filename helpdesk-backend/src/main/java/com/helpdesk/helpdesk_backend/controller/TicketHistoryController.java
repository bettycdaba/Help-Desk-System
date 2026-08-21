package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
public class TicketHistoryController {

    private final TicketHistoryService historyService;

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketAssignmentHistoryResponseDTO>> getAssignmentHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(historyService.getAssignmentHistory(ticketId));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketStatusHistoryResponseDTO>> getStatusHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(historyService.getStatusHistory(ticketId));
    }
}
