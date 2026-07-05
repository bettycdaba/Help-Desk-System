package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.DepartmentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.DepartmentResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Department;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.DepartmentRepository;
import com.helpdesk.helpdesk_backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));
        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));
        departmentRepository.delete(department);
    }

    private DepartmentResponseDTO mapToResponse(Department department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }
}