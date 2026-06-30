package com.helpdesk.helpdesk_backend.dto;

public class TicketCommentRequestDTO {

    private String comment;
    private Long ticketId;
    private Long userId;

    public TicketCommentRequestDTO() {
    }

    public TicketCommentRequestDTO(String comment, Long ticketId, Long userId) {
        this.comment = comment;
        this.ticketId = ticketId;
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}