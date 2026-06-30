package com.helpdesk.helpdesk_backend.dto;

public class TicketAssignmentHistoryRequestDTO {

    private Long ticketId;
    private Long oldAssigneeId;
    private Long newAssigneeId;
    private Long assignedById;

    public TicketAssignmentHistoryRequestDTO() {
    }

    public TicketAssignmentHistoryRequestDTO(Long ticketId, Long oldAssigneeId, Long newAssigneeId, Long assignedById) {
        this.ticketId = ticketId;
        this.oldAssigneeId = oldAssigneeId;
        this.newAssigneeId = newAssigneeId;
        this.assignedById = assignedById;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getOldAssigneeId() {
        return oldAssigneeId;
    }

    public void setOldAssigneeId(Long oldAssigneeId) {
        this.oldAssigneeId = oldAssigneeId;
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