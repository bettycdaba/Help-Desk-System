package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}