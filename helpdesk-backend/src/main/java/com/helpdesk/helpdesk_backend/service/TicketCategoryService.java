package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.TicketCategoryRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketCategoryResponseDTO;

import java.util.List;

public interface TicketCategoryService {

    TicketCategoryResponseDTO createCategory(TicketCategoryRequestDTO dto);

    List<TicketCategoryResponseDTO> getAllCategories();

    TicketCategoryResponseDTO getCategoryById(Long id);

    TicketCategoryResponseDTO updateCategory(Long id, TicketCategoryRequestDTO dto);

    void deleteCategory(Long id);
}