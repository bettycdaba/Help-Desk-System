package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {

    List<TicketAttachment> findByTicketId(Long ticketId);
}