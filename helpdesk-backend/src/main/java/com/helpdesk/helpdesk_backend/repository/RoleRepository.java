package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findBySupervisorsId(Long userId);
    
}