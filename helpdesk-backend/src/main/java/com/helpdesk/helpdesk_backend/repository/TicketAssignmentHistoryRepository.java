package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.TicketAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAssignmentHistoryRepository
        extends JpaRepository<TicketAssignmentHistory, Long> {

    List<TicketAssignmentHistory> findByTicketIdOrderByAssignedAtDesc(Long ticketId);

    boolean existsByTicketId(Long ticketId);

    @Query("SELECT DISTINCT h.assignedBy.id FROM TicketAssignmentHistory h " +
       "WHERE h.ticket.id = :ticketId " +
       "AND h.rejectionReason IS NOT NULL")
List<Long> findOfficerIdsWhoRejectedTicket(@Param("ticketId") Long ticketId);
}

