package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.UserRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getUsersByDepartment(Long departmentId);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    void deleteUser(Long id);
}