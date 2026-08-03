package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.PermissionDTO;
import com.helpdesk.helpdesk_backend.dto.RolePermissionsDTO;
import com.helpdesk.helpdesk_backend.entity.Permission;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.repository.PermissionRepository;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public RolePermissionsDTO getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Long> permissionIds = role.getPermissions()
            .stream()
            .map(Permission::getId)
            .collect(Collectors.toList());

        return RolePermissionsDTO.builder()
            .roleId(role.getId())
            .roleName(role.getName())
            .permissionIds(permissionIds)
            .build();
    }

    @Transactional
    public RolePermissionsDTO updateRolePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new java.util.HashSet<>(permissions));
        roleRepository.save(role);

        return getRolePermissions(roleId);
    }

    private PermissionDTO convertToDTO(Permission permission) {
        return PermissionDTO.builder()
            .id(permission.getId())
            .name(permission.getName())
            .description(permission.getDescription())
            .build();
    }
}