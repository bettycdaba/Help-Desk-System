package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.DepartmentRequestDTO;
import com.helpdesk.helpdesk_backend.dto.DepartmentResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Department;
import com.helpdesk.helpdesk_backend.repository.DepartmentRepository;
import com.helpdesk.helpdesk_backend.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        return toResponseDTO(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Override
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto) {
        Department department = findEntityById(id);
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        return toResponseDTO(departmentRepository.save(department));
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.delete(findEntityById(id));
    }

    private Department findEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + id));
    }

    private DepartmentResponseDTO toResponseDTO(Department department) {
        return new DepartmentResponseDTO(department.getId(), department.getName(), department.getDescription());
    }
}