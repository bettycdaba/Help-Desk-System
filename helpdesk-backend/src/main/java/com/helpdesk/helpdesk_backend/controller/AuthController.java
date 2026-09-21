package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.*;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.exception.BadRequestException;
import com.helpdesk.helpdesk_backend.repository.UserRepository;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        validateActiveUser(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword()));
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(toLoginResponse(user, jwtUtil.generateToken(user), "Login successful"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoginResponseDTO> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toLoginResponse(user, null, null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return messageResponse("Temporary password sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getEmail(), request.getTemporaryPassword(), request.getNewPassword());
        return messageResponse("Password reset successfully");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return messageResponse("Password changed successfully");
    }

    @GetMapping("/must-change-password")
    public ResponseEntity<Map<String, Boolean>> mustChange(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("mustChangePassword", userService.mustChangePassword(email)));
    }

    private void validateActiveUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("Your account has been deactivated. Please contact your administrator.");
        }
    }

    private LoginResponseDTO toLoginResponse(User user, String token, String message) {
        return LoginResponseDTO.builder()
                .id(user.getId()).token(token).email(user.getEmail())
                .firstName(user.getFirstName()).lastName(user.getLastName()).message(message)
                .roles(roleNames(user)).permissions(authorityNames(user)).build();
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).toList();
    }

    private List<String> authorityNames(User user) {
        return user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList();
    }

    private ResponseEntity<Map<String, String>> messageResponse(String message) {
        return ResponseEntity.ok(Map.of("message", message));
    }
}
