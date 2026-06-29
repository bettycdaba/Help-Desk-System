package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

public class TicketStatusHistoryResponseDTO {

    private Long id;
    private Long ticketId;
    private String oldStatus;
    private String newStatus;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;

    public TicketStatusHistoryResponseDTO() {
    }

    public TicketStatusHistoryResponseDTO(Long id, Long ticketId, String oldStatus, String newStatus,
                                           Long changedById, String changedByName, LocalDateTime changedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
        this.changedByName = changedByName;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}