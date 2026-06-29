package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAssignmentHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusHistoryResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketAssignmentHistoryService;
import com.helpdesk.helpdesk_backend.service.TicketStatusHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
public class TicketHistoryController {

    private final TicketAssignmentHistoryService assignmentHistoryService;
    private final TicketStatusHistoryService statusHistoryService;

    public TicketHistoryController(TicketAssignmentHistoryService assignmentHistoryService,
                                    TicketStatusHistoryService statusHistoryService) {
        this.assignmentHistoryService = assignmentHistoryService;
        this.statusHistoryService = statusHistoryService;
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<TicketAssignmentHistoryResponseDTO>> getAssignmentHistory(@PathVariable Long ticketId) {
        return ResponseEntity.ok(assignmentHistoryService.getHistoryByTicket(ticketId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<TicketStatusHistoryResponseDTO>> getStatusHistory(@PathVariable Long ticketId) {
        return ResponseEntity.ok(statusHistoryService.getHistoryByTicket(ticketId));
    }
}