package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.RoleRequestDTO;
import com.helpdesk.helpdesk_backend.dto.RoleResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void createRoleShouldPersistPermissions() {
        RoleRequestDTO request = RoleRequestDTO.builder()
                .name("CUSTOM")
                .description("Custom role")
                .permissions(Set.of("VIEW_ALL_USERS", "VIEW_ALL_TICKETS"))
                .build();

        Role savedRole = Role.builder()
                .id(1L)
                .name("CUSTOM")
                .description("Custom role")
                .permissions(Set.of("VIEW_ALL_USERS", "VIEW_ALL_TICKETS"))
                .build();

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        RoleResponseDTO response = roleService.createRole(request);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());

        assertEquals(Set.of("VIEW_ALL_USERS", "VIEW_ALL_TICKETS"), response.getPermissions());
        assertEquals(Set.of("VIEW_ALL_USERS", "VIEW_ALL_TICKETS"), captor.getValue().getPermissions());
    }
}
