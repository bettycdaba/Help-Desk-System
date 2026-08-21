package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.UserRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UserResponseDTO;
import com.helpdesk.helpdesk_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_USERS', 'ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

@GetMapping("/{id}")
@PreAuthorize("hasAnyAuthority('VIEW_USERS', 'ROLE_ADMIN') || #id == authentication.principal.id")
public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
}

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyAuthority('VIEW_USERS', 'ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(userService.getUsersByDepartment(departmentId));
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<UserResponseDTO> updateFullUser(
    // @PathVariable Long id,
    // @RequestBody UserRequestDTO request) {// @Valid

    // return ResponseEntity.ok(userService.updateUser(id, request));
    // }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EDIT_USER', 'ROLE_ADMIN') || #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDTO request) {// @Valid

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DELETE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

@GetMapping("/active")
@PreAuthorize("hasAnyAuthority('VIEW_USERS', 'ROLE_ADMIN', 'ROLE_SUPERVISOR')")
public ResponseEntity<List<UserResponseDTO>> getActiveUsers() {
    return ResponseEntity.ok(userService.getActiveUsers());
}

@GetMapping("/support-officers")
@PreAuthorize("hasAnyAuthority('VIEW_USERS', 'ROLE_ADMIN', 'ROLE_SUPERVISOR')")
public ResponseEntity<List<UserResponseDTO>> getSupportOfficers() {
    return ResponseEntity.ok(userService.getActiveSupportOfficers());
}
@GetMapping("/support-officers/workload")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
public ResponseEntity<List<Map<String, Object>>> getSupportOfficerWorkload() {
    return ResponseEntity.ok(userService.getSupportOfficerWorkload());
}
}
