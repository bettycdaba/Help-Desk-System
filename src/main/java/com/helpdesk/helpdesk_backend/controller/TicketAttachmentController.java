package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAttachmentResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketAttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/attachments")
public class TicketAttachmentController {

    private final TicketAttachmentService attachmentService;

    public TicketAttachmentController(TicketAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<TicketAttachmentResponseDTO> uploadAttachment(@PathVariable Long ticketId,
                                                                         @RequestParam("file") MultipartFile file,
                                                                         @RequestParam("uploadedById") Long uploadedById) {
        return new ResponseEntity<>(attachmentService.uploadAttachment(ticketId, uploadedById, file), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketAttachmentResponseDTO>> getAttachmentsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByTicket(ticketId));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long ticketId, @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }
}