package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class TicketCategoryController {

    private final TicketCategoryService categoryService;

    public TicketCategoryController(TicketCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<TicketCategoryResponseDTO> createCategory(@RequestBody TicketCategoryRequestDTO dto) {
        return new ResponseEntity<>(categoryService.createCategory(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketCategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketCategoryResponseDTO> updateCategory(@PathVariable Long id,
                                                                      @RequestBody TicketCategoryRequestDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}