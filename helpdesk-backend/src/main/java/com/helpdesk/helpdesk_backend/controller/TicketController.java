package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAssignRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketStatusUpdateRequestDTO;
import com.helpdesk.helpdesk_backend.entity.TicketStatus;
import com.helpdesk.helpdesk_backend.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@RequestBody TicketRequestDTO dto) {
        return new ResponseEntity<>(ticketService.createTicket(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketResponseDTO> getTicketByTicketNumber(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ticketService.getTicketByTicketNumber(ticketNumber));
    }

    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByCreatedBy(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByCreatedBy(userId));
    }

    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByAssignedTo(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByAssignedTo(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(@PathVariable Long id, @RequestBody TicketRequestDTO dto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, dto));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponseDTO> assignTicket(@PathVariable Long id,
                                                            @RequestBody TicketAssignRequestDTO dto) {
        return ResponseEntity.ok(ticketService.assignTicket(id, dto.getNewAssigneeId(), dto.getAssignedById()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> updateStatus(@PathVariable Long id,
                                                            @RequestBody TicketStatusUpdateRequestDTO dto) {
        return ResponseEntity.ok(ticketService.updateStatus(id, dto.getNewStatus(), dto.getChangedById()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}