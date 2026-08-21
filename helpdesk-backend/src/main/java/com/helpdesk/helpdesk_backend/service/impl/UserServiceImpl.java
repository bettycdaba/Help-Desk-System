package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.UserRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UserResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Department;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.entity.enums.TicketStatus;
import com.helpdesk.helpdesk_backend.exception.BadRequestException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.repository.DepartmentRepository;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.service.EmailService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Map;

import com.helpdesk.helpdesk_backend.repository.TicketRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TicketRepository ticketRepository;

   @Override
@Transactional
public UserResponseDTO createUser(UserRequestDTO request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BadRequestException(
                "A user with this email already exists: " + request.getEmail());
    }

    if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
        throw new BadRequestException(
                "A user with this employee ID already exists: " + request.getEmployeeId());
    }

    Department department = departmentRepository.findById(request.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Department not found with id: " + request.getDepartmentId()));

    Set<Role> roles = resolveRoles(request.getRoleIds());
    
    // If no roles selected, assign default EMPLOYEE role
    if (roles.isEmpty()) {
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
            .orElseThrow(() -> new ResourceNotFoundException(
                "Default EMPLOYEE role not found"));
        roles.add(employeeRole);
    }

    User user = User.builder()
            .employeeId(request.getEmployeeId())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .active(request.getActive() != null 
                ? request.getActive() : true)
            .password(passwordEncoder.encode(request.getPassword()))
            .department(department)
            .roles(roles)
            .mustChangePassword(false)
            .build();

    return mapToResponse(userRepository.save(user));
}
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException(
                    "Department not found with id: " + departmentId);
        }
        return userRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "A user with this email already exists: " + request.getEmail());
        }

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean hasPrivileges = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("EDIT_USER") || a.getAuthority().equals("ROLE_ADMIN"));

        if (hasPrivileges) {
            if (request.getDepartmentId() != null) {
                Department department = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Department not found with id: " + request.getDepartmentId()));
                user.setDepartment(department);
            }
            if (request.getActive() != null) {
                user.setActive(request.getActive());
            }
            if (request.getRoleIds() != null) {
                user.setRoles(resolveRoles(request.getRoleIds()));
            }
        }

        user.setEmployeeId(request.getEmployeeId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        userRepository.delete(user);
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
    return new HashSet<>(roles);
    }

    private UserResponseDTO mapToResponse(User user) {
        Set<Long> roleIds = user.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserResponseDTO.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .active(user.getActive())
                .departmentId(user.getDepartment().getId())
                .departmentName(user.getDepartment().getName())
                .roleIds(roleIds)
                .roleNames(roleNames)
                .build();
    }

    @Override
public void forgotPassword(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No account found with email: " + email));

    String tempPassword = generateTempPassword();

    // TEMPORARY
    System.out.println("=== TEMP PASSWORD FOR TESTING ===");
    System.out.println("Email: " + email);
    System.out.println("Temp Password: " + tempPassword);
    System.out.println("=================================");
    // this will be removed later, but for now, it helps to see the temp password in the console for testing purposes.

    user.setPassword(
        passwordEncoder.encode(tempPassword));
    user.setMustChangePassword(true);
    userRepository.save(user);

    emailService.sendPasswordResetEmail(
        email,
        user.getFirstName() + " " + user.getLastName(),
        tempPassword
    );
}

@Override
public void resetPassword(String email,
    String temporaryPassword, String newPassword) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(
            "User not found"));

    if (!passwordEncoder.matches(
        temporaryPassword, user.getPassword())) {
        throw new BadRequestException(
            "Temporary password is incorrect");
    }

    user.setPassword(
        passwordEncoder.encode(newPassword));
    user.setMustChangePassword(false);
    userRepository.save(user);
}

@Override
public boolean mustChangePassword(String email) {
    return userRepository.findByEmail(email)
        .map(User::getMustChangePassword)
        .orElse(false);
}

private String generateTempPassword() {
    String chars = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder();
    java.util.Random random = new java.util.Random();
    for (int i = 0; i < 10; i++) {
        sb.append(chars.charAt(
            random.nextInt(chars.length())));
    }
    return sb.toString();
}

@Override
public List<UserResponseDTO> getActiveUsers() {
    return userRepository.findByActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}

@Override
@Transactional
public UserResponseDTO registerUser(UserRequestDTO request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BadRequestException(
                "A user with this email already exists: "
                        + request.getEmail());
    }

    if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
        throw new BadRequestException(
                "A user with this employee ID already exists: "
                        + request.getEmployeeId());
    }

    Department department = departmentRepository
            .findById(request.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Department not found with id: "
                            + request.getDepartmentId()));

    // ALWAYS assign EMPLOYEE during public registration.
    // Ignore any roleIds sent by the client.
    Role employeeRole = roleRepository
            .findByName("EMPLOYEE")
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Default EMPLOYEE role not found"));

    Set<Role> roles = new HashSet<>();
    roles.add(employeeRole);

    User user = User.builder()
            .employeeId(request.getEmployeeId())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .active(true)
            .password(passwordEncoder.encode(request.getPassword()))
            .department(department)
            .roles(roles)
            .mustChangePassword(false)
            .build();

    return mapToResponse(userRepository.save(user));
}

@Override
@Transactional(readOnly = true)
public List<UserResponseDTO> getActiveSupportOfficers() {
    return userRepository.findActiveSupportOfficers()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}

@Override
public List<Map<String, Object>> getSupportOfficerWorkload() {
    return userRepository.findActiveSupportOfficers().stream().map(officer -> {
        Map<String, Object> map = new HashMap<>();
        map.put("id", officer.getId());
        map.put("firstName", officer.getFirstName());
        map.put("lastName", officer.getLastName());
        map.put("activeTicketCount", ticketRepository.findByAssignedToId(officer.getId()).stream()
                .filter(t -> t.getStatus() == TicketStatus.ASSIGNED || t.getStatus() == TicketStatus.IN_PROGRESS)
                .count());
        return map;
    }).collect(Collectors.toList());
}
}

