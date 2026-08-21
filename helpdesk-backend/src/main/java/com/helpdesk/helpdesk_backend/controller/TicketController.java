package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.*;
import com.helpdesk.helpdesk_backend.entity.enums.TicketPriority;
import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;
import com.helpdesk.helpdesk_backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE_TICKET', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketRequestDTO request) {
        return new ResponseEntity<>(
                ticketService.createTicket(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> getTicketByNumber(
            @PathVariable String ticketNumber) {
        return ResponseEntity.ok(ticketService.getTicketByNumber(ticketNumber));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByStatus(
            @PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByPriority(
            @PathVariable TicketPriority priority) {
        return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
    }

    @GetMapping("/created-by/{userId}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByCreatedBy(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByCreatedBy(userId));
    }

    @GetMapping("/assigned-to/{userId}")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByAssignedTo(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByAssignedTo(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPDATE_TICKET_STATUS', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketRequestDTO request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ASSIGN_TICKET', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketAssignRequestDTO request) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('UPDATE_TICKET_STATUS', 'ROLE_ADMIN')")
public ResponseEntity<TicketResponseDTO> rejectTicket(
        @PathVariable Long id,
        @Valid @RequestBody TicketRejectionRequestDTO request) {

    return ResponseEntity.ok(
            ticketService.rejectTicket(id, request));
}

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('UPDATE_TICKET_STATUS', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/workload")
    @PreAuthorize("hasAnyAuthority('VIEW_TICKETS', 'ROLE_ADMIN')")
public ResponseEntity<List<TeamWorkloadDTO>> getTeamWorkload() {
    return ResponseEntity.ok(ticketService.getTeamWorkload());
}
}

