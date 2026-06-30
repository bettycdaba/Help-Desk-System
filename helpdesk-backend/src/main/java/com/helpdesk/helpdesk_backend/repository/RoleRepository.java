package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}