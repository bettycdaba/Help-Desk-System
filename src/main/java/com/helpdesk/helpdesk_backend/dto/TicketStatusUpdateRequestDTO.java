package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.entity.TicketStatus;

public class TicketStatusUpdateRequestDTO {

    private TicketStatus newStatus;
    private Long changedById;

    public TicketStatusUpdateRequestDTO() {
    }

    public TicketStatusUpdateRequestDTO(TicketStatus newStatus, Long changedById) {
        this.newStatus = newStatus;
        this.changedById = changedById;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedById() {
        return changedById;
    }

    public void setChangedById(Long changedById) {
        this.changedById = changedById;
    }
}