package com.helpdesk.helpdesk_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_assignment_history")
public class TicketAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_assignee_id", nullable = false)
    private User oldAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assignee_id", nullable = false)
    private User newAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    public TicketAssignmentHistory() {
    }

    public TicketAssignmentHistory(Long id, Ticket ticket, User oldAssignee, User newAssignee,
                                    User assignedBy, LocalDateTime assignedAt) {
        this.id = id;
        this.ticket = ticket;
        this.oldAssignee = oldAssignee;
        this.newAssignee = newAssignee;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getOldAssignee() {
        return oldAssignee;
    }

    public void setOldAssignee(User oldAssignee) {
        this.oldAssignee = oldAssignee;
    }

    public User getNewAssignee() {
        return newAssignee;
    }

    public void setNewAssignee(User newAssignee) {
        this.newAssignee = newAssignee;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}