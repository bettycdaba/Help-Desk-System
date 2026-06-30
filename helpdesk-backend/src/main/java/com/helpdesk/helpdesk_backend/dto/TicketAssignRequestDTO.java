package com.helpdesk.helpdesk_backend.dto;

public class TicketAssignRequestDTO {

    private Long newAssigneeId;
    private Long assignedById;

    public TicketAssignRequestDTO() {
    }

    public TicketAssignRequestDTO(Long newAssigneeId, Long assignedById) {
        this.newAssigneeId = newAssigneeId;
        this.assignedById = assignedById;
    }

    public Long getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setNewAssigneeId(Long newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }
}