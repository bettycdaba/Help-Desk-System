package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.entity.TicketPriority;

public class TicketRequestDTO {

    private String subject;
    private String description;
    private TicketPriority priority;
    private Long categoryId;
    private Long createdById;
    private Long assignedToId;

    public TicketRequestDTO() {
    }

    public TicketRequestDTO(String subject, String description, TicketPriority priority,
                             Long categoryId, Long createdById, Long assignedToId) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.categoryId = categoryId;
        this.createdById = createdById;
        this.assignedToId = assignedToId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }
}