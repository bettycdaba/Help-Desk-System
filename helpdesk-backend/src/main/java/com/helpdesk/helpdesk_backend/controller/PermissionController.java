package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.PermissionDTO;
import com.helpdesk.helpdesk_backend.dto.RolePermissionsDTO;
import com.helpdesk.helpdesk_backend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<RolePermissionsDTO> getRolePermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(permissionService.getRolePermissions(roleId));
    }

    @PutMapping("/role/{roleId}")
    public ResponseEntity<RolePermissionsDTO> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        return ResponseEntity.ok(
            permissionService.updateRolePermissions(roleId, permissionIds)
        );
    }
}