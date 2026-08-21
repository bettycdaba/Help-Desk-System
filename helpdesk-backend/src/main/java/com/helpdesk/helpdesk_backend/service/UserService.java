package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.UserRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UserResponseDTO;

import java.util.List;

import java.util.Map;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO request);

    UserResponseDTO registerUser(UserRequestDTO request);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getUsersByDepartment(Long departmentId);

    UserResponseDTO updateUser(Long id, UserRequestDTO request);

    void deleteUser(Long id);

    void forgotPassword(String email);
    void resetPassword(String email, 
                   String temporaryPassword, 
                   String newPassword);
    boolean mustChangePassword(String email);

    List<UserResponseDTO> getActiveUsers();

    List<UserResponseDTO> getActiveSupportOfficers();

    List<Map<String, Object>> getSupportOfficerWorkload();
    
}