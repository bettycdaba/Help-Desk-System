package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {
}