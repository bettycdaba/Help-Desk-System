package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;

import java.util.List;

public interface TicketCategoryService {

    TicketCategoryResponseDTO createCategory(TicketCategoryRequestDTO request);

    List<TicketCategoryResponseDTO> getAllCategories();

    TicketCategoryResponseDTO getCategoryById(Long id);

    TicketCategoryResponseDTO updateCategory(Long id, TicketCategoryRequestDTO request);

    TicketCategoryResponseDTO updateCategoryStatus(Long id, boolean active);

    void deleteCategory(Long id);
}