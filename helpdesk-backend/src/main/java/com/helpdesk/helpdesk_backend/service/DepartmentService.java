package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.DepartmentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.DepartmentResponseDTO;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO request);

    List<DepartmentResponseDTO> getAllDepartments();

    DepartmentResponseDTO getDepartmentById(Long id);

    DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request);

    void deleteDepartment(Long id);
}