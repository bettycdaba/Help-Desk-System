package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.TicketAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAssignmentHistoryRepository extends JpaRepository<TicketAssignmentHistory, Long> {

    List<TicketAssignmentHistory> findByTicketIdOrderByAssignedAtDesc(Long ticketId);
}