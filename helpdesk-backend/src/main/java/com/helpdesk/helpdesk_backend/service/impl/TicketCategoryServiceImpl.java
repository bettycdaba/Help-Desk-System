package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketCategory;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.TicketCategoryRepository;
import com.helpdesk.helpdesk_backend.service.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;

    @Override
    @Transactional
    public TicketCategoryResponseDTO createCategory(TicketCategoryRequestDTO request) {
        TicketCategory category = TicketCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponseDTO getCategoryById(Long id) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public TicketCategoryResponseDTO updateCategory(Long id, TicketCategoryRequestDTO request) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        TicketCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    private TicketCategoryResponseDTO mapToResponse(TicketCategory category) {
        return TicketCategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}