package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;
import com.helpdesk.helpdesk_backend.entity.TicketCategory;
import com.helpdesk.helpdesk_backend.repository.TicketCategoryRepository;
import com.helpdesk.helpdesk_backend.service.TicketCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryRepository categoryRepository;

    public TicketCategoryServiceImpl(TicketCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public TicketCategoryResponseDTO createCategory(TicketCategoryRequestDTO dto) {
        TicketCategory category = new TicketCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toResponseDTO(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketCategoryResponseDTO getCategoryById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Override
    public TicketCategoryResponseDTO updateCategory(Long id, TicketCategoryRequestDTO dto) {
        TicketCategory category = findEntityById(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return toResponseDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.delete(findEntityById(id));
    }

    private TicketCategory findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket category not found with id: " + id));
    }

    private TicketCategoryResponseDTO toResponseDTO(TicketCategory category) {
        return new TicketCategoryResponseDTO(category.getId(), category.getName(), category.getDescription());
    }
}