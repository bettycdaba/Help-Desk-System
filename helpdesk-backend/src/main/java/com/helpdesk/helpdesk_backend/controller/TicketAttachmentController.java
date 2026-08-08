package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketAttachmentResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Ticket;
import com.helpdesk.helpdesk_backend.entity.TicketAttachment;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.TicketAttachmentRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets/{ticketId}/attachments")
@RequiredArgsConstructor
public class TicketAttachmentController {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketAttachmentResponseDTO> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedById") Long uploadedById) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with id: " + ticketId));

        User uploadedBy = userRepository.findById(uploadedById)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + uploadedById));

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).build();
        }

        try {
            Path uploadPath = Paths.get(uploadDir)
                    .resolve("ticket-" + ticketId);
            Files.createDirectories(uploadPath);

            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && 
                originalFileName.contains(".")) {
                extension = originalFileName.substring(
                    originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID()
                    .toString() + extension;

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            TicketAttachment attachment = TicketAttachment.builder()
                    .fileName(originalFileName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .ticket(ticket)
                    .uploadedBy(uploadedBy)
                    .build();

            TicketAttachment saved = 
                attachmentRepository.save(attachment);

            return new ResponseEntity<>(
                mapToResponse(saved), HttpStatus.CREATED);

        } catch (IOException e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TicketAttachmentResponseDTO>> 
        getAttachments(@PathVariable Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException(
                "Ticket not found with id: " + ticketId);
        }

        List<TicketAttachmentResponseDTO> attachments =
            attachmentRepository.findByTicketId(ticketId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long ticketId,
            @PathVariable Long attachmentId) {

        TicketAttachment attachment = 
            attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment not found"));

        try {
            Path filePath = Paths.get(
                attachment.getFilePath());
            Resource resource = new UrlResource(
                filePath.toUri());

            if (!resource.exists() || 
                !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(
                filePath);
            if (contentType == null) {
                contentType = 
                    "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                        contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                            + attachment.getFileName()
                            + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long ticketId,
            @PathVariable Long attachmentId) {

        TicketAttachment attachment = 
            attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attachment not found"));

        try {
            Path filePath = Paths.get(
                attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but continue with DB deletion
        }

        attachmentRepository.delete(attachment);
        return ResponseEntity.noContent().build();
    }

    private TicketAttachmentResponseDTO mapToResponse(
            TicketAttachment attachment) {

        String fileName = attachment.getFileName();
        String fileType = "file";
        if (fileName != null && fileName.contains(".")) {
            fileType = fileName.substring(
                fileName.lastIndexOf(".") + 1)
                .toLowerCase();
        }

        return TicketAttachmentResponseDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .fileType(fileType)
                .uploadedAt(attachment.getUploadedAt())
                .ticketId(attachment.getTicket().getId())
                .uploadedById(
                    attachment.getUploadedBy().getId())
                .uploadedByName(
                    attachment.getUploadedBy().getFirstName()
                    + " " + 
                    attachment.getUploadedBy().getLastName())
                .downloadUrl("/api/tickets/" 
                    + attachment.getTicket().getId()
                    + "/attachments/" 
                    + attachment.getId() 
                    + "/download")
                .build();
    }
}