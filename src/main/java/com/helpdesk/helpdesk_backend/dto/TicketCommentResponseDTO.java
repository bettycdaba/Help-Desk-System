package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

public class TicketCommentResponseDTO {

    private Long id;
    private String comment;
    private LocalDateTime commentedAt;
    private Long ticketId;
    private Long userId;
    private String userName;

    public TicketCommentResponseDTO() {
    }

    public TicketCommentResponseDTO(Long id, String comment, LocalDateTime commentedAt,
                                     Long ticketId, Long userId, String userName) {
        this.id = id;
        this.comment = comment;
        this.commentedAt = commentedAt;
        this.ticketId = ticketId;
        this.userId = userId;
        this.userName = userName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}