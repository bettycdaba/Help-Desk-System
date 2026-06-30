package com.helpdesk.helpdesk_backend.dto;

public class TicketStatusHistoryRequestDTO {

    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private Long changedById;

    public TicketStatusHistoryRequestDTO() {
    }

    public TicketStatusHistoryRequestDTO(Long ticketId, String oldStatus, String newStatus, Long changedById) {
        this.ticketId = ticketId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedById() {
        return changedById;
    }

    public void setChangedById(Long changedById) {
        this.changedById = changedById;
    }
}