package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

public class TicketAssignmentHistoryResponseDTO {

    private Long id;
    private Long ticketId;
    private Long oldAssigneeId;
    private String oldAssigneeName;
    private Long newAssigneeId;
    private String newAssigneeName;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedAt;

    public TicketAssignmentHistoryResponseDTO() {
    }

    public TicketAssignmentHistoryResponseDTO(Long id, Long ticketId, Long oldAssigneeId, String oldAssigneeName,
                                               Long newAssigneeId, String newAssigneeName, Long assignedById,
                                               String assignedByName, LocalDateTime assignedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.oldAssigneeId = oldAssigneeId;
        this.oldAssigneeName = oldAssigneeName;
        this.newAssigneeId = newAssigneeId;
        this.newAssigneeName = newAssigneeName;
        this.assignedById = assignedById;
        this.assignedByName = assignedByName;
        this.assignedAt = assignedAt;
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

    public Long getOldAssigneeId() {
        return oldAssigneeId;
    }

    public void setOldAssigneeId(Long oldAssigneeId) {
        this.oldAssigneeId = oldAssigneeId;
    }

    public String getOldAssigneeName() {
        return oldAssigneeName;
    }

    public void setOldAssigneeName(String oldAssigneeName) {
        this.oldAssigneeName = oldAssigneeName;
    }

    public Long getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setNewAssigneeId(Long newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public String getNewAssigneeName() {
        return newAssigneeName;
    }

    public void setNewAssigneeName(String newAssigneeName) {
        this.newAssigneeName = newAssigneeName;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}