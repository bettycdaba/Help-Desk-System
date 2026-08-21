package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGE_CATEGORIES', 'ROLE_ADMIN')")
    public ResponseEntity<TicketCategoryResponseDTO> createCategory(
            @Valid @RequestBody TicketCategoryRequestDTO request) {
        return new ResponseEntity<>(
                categoryService.createCategory(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_CATEGORIES', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VIEW_CATEGORIES', 'ROLE_ADMIN')")
    public ResponseEntity<TicketCategoryResponseDTO> getCategoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGE_CATEGORIES', 'ROLE_ADMIN')")
    public ResponseEntity<TicketCategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TicketCategoryRequestDTO request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGE_CATEGORIES', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
