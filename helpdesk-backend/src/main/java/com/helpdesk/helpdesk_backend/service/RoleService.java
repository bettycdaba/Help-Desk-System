package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.RoleRequestDTO;
import com.helpdesk.helpdesk_backend.dto.RoleResponseDTO;

import java.util.List;

public interface RoleService {

    RoleResponseDTO createRole(RoleRequestDTO request);

    List<RoleResponseDTO> getAllRoles();

    RoleResponseDTO getRoleById(Long id);

    RoleResponseDTO updateRole(Long id, RoleRequestDTO request);

    void deleteRole(Long id);
}