package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.RoleRequestDTO;
import com.helpdesk.helpdesk_backend.dto.RoleResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.exception.BadRequestException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        Set<User> supervisors = resolveSupervisors(request.getSupervisorIds(), null);
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
            .supervisors(supervisors)
                .build();
        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRoleStatus(Long id, boolean active) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
        role.setActive(active);
        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponseDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
        return mapToResponse(role);
    }

    @Override
    @Transactional
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
                role.setSupervisors(resolveSupervisors(request.getSupervisorIds(), id));
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + id));
        roleRepository.delete(role);
    }

    private RoleResponseDTO mapToResponse(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .supervisorIds(role.getSupervisors().stream()
                    .map(User::getId).collect(Collectors.toSet()))
                .supervisorNames(role.getSupervisors().stream()
                    .map(user -> user.getFirstName() + " " + user.getLastName())
                    .collect(Collectors.toSet()))
                .build();
    } 

            private Set<User> resolveSupervisors(Set<Long> supervisorIds, Long currentRoleId) {
            Set<Long> ids = supervisorIds == null ? Set.of() : supervisorIds;
            if (ids.size() > 2) {
                throw new BadRequestException("A role can have at most two supervisors.");
            }

            Set<User> supervisors = new HashSet<>(userRepository.findAllById(ids));
            if (supervisors.size() != ids.size()) {
                throw new ResourceNotFoundException("One or more supervisors were not found.");
            }

            for (User supervisor : supervisors) {
                boolean isSupervisor = supervisor.getRoles().stream()
                    .anyMatch(userRole -> "SUPERVISOR".equals(userRole.getName()));
                if (!isSupervisor) {
                throw new BadRequestException(
                    supervisor.getFirstName() + " " + supervisor.getLastName()
                        + " does not have the SUPERVISOR role.");
                }

                long assignedRoleCount = roleRepository.findBySupervisorsId(supervisor.getId())
                    .stream()
                    .filter(existingRole -> currentRoleId == null
                        || !existingRole.getId().equals(currentRoleId))
                    .count();
                if (assignedRoleCount >= 2) {
                throw new BadRequestException(
                    supervisor.getFirstName() + " " + supervisor.getLastName()
                        + " already manages two roles.");
                }
            }
            return supervisors;
            }
}

