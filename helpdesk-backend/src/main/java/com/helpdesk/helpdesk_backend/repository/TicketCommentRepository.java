package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicketIdOrderByCommentedAtAsc(Long ticketId);
}