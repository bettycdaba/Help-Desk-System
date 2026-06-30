package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByEmail(String email);
}