package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

public class TicketAttachmentResponseDTO {

    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private Long ticketId;
    private Long uploadedById;
    private String uploadedByName;

    public TicketAttachmentResponseDTO() {
    }

    public TicketAttachmentResponseDTO(Long id, String fileName, String filePath, Long fileSize,
                                        LocalDateTime uploadedAt, Long ticketId, Long uploadedById,
                                        String uploadedByName) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.ticketId = ticketId;
        this.uploadedById = uploadedById;
        this.uploadedByName = uploadedByName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }
}