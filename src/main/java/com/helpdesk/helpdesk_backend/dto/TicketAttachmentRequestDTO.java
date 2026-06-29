package com.helpdesk.helpdesk_backend.dto;

public class TicketAttachmentRequestDTO {

    private String fileName;
    private String filePath;
    private Long fileSize;
    private Long ticketId;
    private Long uploadedById;

    public TicketAttachmentRequestDTO() {
    }

    public TicketAttachmentRequestDTO(String fileName, String filePath, Long fileSize,
                                       Long ticketId, Long uploadedById) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.ticketId = ticketId;
        this.uploadedById = uploadedById;
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
}