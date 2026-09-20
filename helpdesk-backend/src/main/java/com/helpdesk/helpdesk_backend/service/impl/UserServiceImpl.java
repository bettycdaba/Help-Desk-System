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
import com.helpdesk.helpdesk_backend.service.NotificationService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.regex.Pattern;

import com.helpdesk.helpdesk_backend.repository.TicketRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {

        validateEmail(request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(
                    "A user with this email already exists: " + request.getEmail());
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException(
                    "A user with this employee ID already exists: " + request.getEmployeeId());
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException(
                "A user with this phone number already exists: " + request.getPhoneNumber());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

        if (Boolean.FALSE.equals(department.getActive())) {
            throw new BadRequestException(
                "Cannot create an account in an inactive department.");
        }

        Set<Role> roles = resolveRoles(request.getRoleIds());

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

        User saved = userRepository.save(user);

        notifyNewRoles(saved, roles.stream()
            .filter(role -> !"EMPLOYEE".equals(role.getName()))
            .collect(Collectors.toSet()));

        // 👇 Notify admins
        notifyAdmins(saved, "New account created");

        return mapToResponse(saved);
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
        Set<String> previousRoleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        Set<Role> newlyAssignedRoles = Set.of();

        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
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
                Set<Role> updatedRoles = resolveRoles(request.getRoleIds());
                newlyAssignedRoles = updatedRoles.stream()
                        .filter(role -> !previousRoleNames.contains(role.getName()))
                        .collect(Collectors.toSet());
                user.setRoles(updatedRoles);
            }
        } else {
            validateEmail(request.getEmail());
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException(
                        "A user with this email already exists: " + request.getEmail());
            }
                    if (!user.getPhoneNumber().equals(request.getPhoneNumber()) &&
                        userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    throw new BadRequestException(
                        "A user with this phone number already exists: "
                            + request.getPhoneNumber());
                    }

            user.setEmployeeId(request.getEmployeeId());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User saved = userRepository.save(user);
        notifyNewRoles(saved, newlyAssignedRoles);
        return mapToResponse(saved);
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BadRequestException("Email must be a valid email address");
        }
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        Set<Long> requestedIds = roleIds == null ? Set.of() : roleIds;
        List<Role> requestedRoles = roleRepository.findAllById(requestedIds);
        if (requestedRoles.size() != requestedIds.size()) {
            throw new ResourceNotFoundException("One or more roles were not found.");
        }

        long additionalRoleCount = requestedRoles.stream()
                .filter(role -> !"EMPLOYEE".equals(role.getName()))
                .count();
        if (additionalRoleCount > 1) {
            throw new BadRequestException(
                    "A user can have the EMPLOYEE role and at most one additional role.");
        }

        if (requestedRoles.stream().anyMatch(role -> Boolean.FALSE.equals(role.getActive()))) {
            throw new BadRequestException("Deactivated roles cannot be assigned.");
        }

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default EMPLOYEE role not found"));
        Set<Role> roles = new HashSet<>(requestedRoles);
        roles.add(employeeRole);
        return roles;
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

        System.out.println("=== TEMP PASSWORD FOR TESTING ===");
        System.out.println("Email: " + email);
        System.out.println("Temp Password: " + tempPassword);
        System.out.println("=================================");

        user.setPassword(passwordEncoder.encode(tempPassword));
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
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(temporaryPassword, user.getPassword())) {
            throw new BadRequestException("Temporary password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
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
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
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

        validateEmail(request.getEmail());

        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null) {
            if (Boolean.TRUE.equals(existingUser.getActive())) {
                throw new BadRequestException(
                    "This account is already active. You can log in.");
            }
        }

        User userByEmployeeId = userRepository.findByEmployeeId(request.getEmployeeId()).orElse(null);
        if (userByEmployeeId != null && (existingUser == null || !userByEmployeeId.getId().equals(existingUser.getId()))) {
            throw new BadRequestException(
                    "A user with this employee ID already exists: " + request.getEmployeeId());
        }

        User userByPhone = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        if (userByPhone != null && (existingUser == null || !userByPhone.getId().equals(existingUser.getId()))) {
            throw new BadRequestException(
                "A user with this phone number already exists: " + request.getPhoneNumber());
        }

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()));

        if (Boolean.FALSE.equals(department.getActive())) {
            throw new BadRequestException(
                "Cannot create an account in an inactive department.");
        }

        Role employeeRole = roleRepository
                .findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default EMPLOYEE role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);

        User userToSave;
        if (existingUser != null) {
            existingUser.setEmployeeId(request.getEmployeeId());
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            existingUser.setPhoneNumber(request.getPhoneNumber());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setDepartment(department);
            existingUser.setRoles(roles);
            existingUser.setActive(true);
            userToSave = existingUser;
        } else {
            userToSave = User.builder()
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
        }

        User saved = userRepository.save(userToSave);

        notifyNewRoles(saved, roles.stream()
            .filter(role -> !"EMPLOYEE".equals(role.getName()))
            .collect(Collectors.toSet()));

        // 👇 Notify admins
        notifyAdmins(saved, "New account created via registration");

        return mapToResponse(saved);
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

    // =============================================
    // NOTIFY ADMINS
    // =============================================

    private void notifyAdmins(User newUser, String title) {
        List<User> admins = userRepository.findAll()
                .stream()
                .filter(User::getActive)
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equals(role.getName())))
                .collect(Collectors.toList());

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    null,
                    title + ": " + newUser.getFirstName() + " " 
                        + newUser.getLastName() + " (" + newUser.getEmail() + ")",
                    "new_user"
            );
        }
    }

    private void notifyNewRoles(User user, Set<Role> newlyAssignedRoles) {
        for (Role role : newlyAssignedRoles) {
            notificationService.createNotification(
                    user.getId(),
                    null,
                    "You have been assigned the " + role.getName() + " role.",
                    "role_assigned"
            );
        }
    }
}