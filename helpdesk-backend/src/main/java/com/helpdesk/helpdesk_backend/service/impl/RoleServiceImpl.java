package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.RoleRequestDTO;
import com.helpdesk.helpdesk_backend.dto.RoleResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import com.helpdesk.helpdesk_backend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO request) {
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
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
                .build();
    }
}