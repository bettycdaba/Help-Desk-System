package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketAttachmentResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Ticket;
import com.helpdesk.helpdesk_backend.entity.TicketAttachment;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.repository.TicketAttachmentRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.TicketAttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketAttachmentServiceImpl implements TicketAttachmentService {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public TicketAttachmentServiceImpl(TicketAttachmentRepository attachmentRepository,
                                        TicketRepository ticketRepository,
                                        UserRepository userRepository) {
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TicketAttachmentResponseDTO uploadAttachment(Long ticketId, Long uploadedById, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));
        User uploadedBy = userRepository.findById(uploadedById)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + uploadedById));

        try {
            Path directory = Paths.get(uploadDir, "ticket-" + ticketId);
            Files.createDirectories(directory);

            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = directory.resolve(storedFileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            TicketAttachment attachment = new TicketAttachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(destination.toString());
            attachment.setFileSize(file.getSize());
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setTicket(ticket);
            attachment.setUploadedBy(uploadedBy);

            return toResponseDTO(attachmentRepository.save(attachment));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketAttachmentResponseDTO> getAttachmentsByTicket(Long ticketId) {
        return attachmentRepository.findByTicketId(ticketId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NoSuchElementException("Attachment not found with id: " + attachmentId));
        attachmentRepository.delete(attachment);
    }

    private TicketAttachmentResponseDTO toResponseDTO(TicketAttachment attachment) {
        return new TicketAttachmentResponseDTO(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFilePath(),
                attachment.getFileSize(),
                attachment.getUploadedAt(),
                attachment.getTicket().getId(),
                attachment.getUploadedBy().getId(),
                attachment.getUploadedBy().getFirstName() + " " + attachment.getUploadedBy().getLastName()
        );
    }
}