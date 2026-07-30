package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.ForgotPasswordRequest;
import com.helpdesk.helpdesk_backend.dto.LoginRequestDTO;
import com.helpdesk.helpdesk_backend.dto.LoginResponseDTO;
import com.helpdesk.helpdesk_backend.dto.ResetPasswordRequest;
import com.helpdesk.helpdesk_backend.dto.UserRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UserResponseDTO;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.entity.User;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRequestDTO request) {
        return new ResponseEntity<>(
                userService.createUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);

        LoginResponseDTO response = LoginResponseDTO.builder()
        .id(user.getId())
        .token(token)
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .message("Login successful")
        .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toList()))
        .build();

        return ResponseEntity.ok(response);
    }
        @PostMapping("/forgot-password")
        public ResponseEntity<Map<String, String>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("message",
                "Temporary password sent to your email");
        return ResponseEntity.ok(response);
        }

        @PostMapping("/reset-password")
        public ResponseEntity<Map<String, String>> resetPassword(
        @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(
                request.getEmail(),
                request.getTemporaryPassword(),
                request.getNewPassword()
        );
        Map<String, String> response = new HashMap<>();
        response.put("message",
                "Password reset successfully");
        return ResponseEntity.ok(response);
        }

        @GetMapping("/must-change-password")
        public ResponseEntity<Map<String, Boolean>> mustChange(
        @RequestParam String email) {
        boolean must = userService.mustChangePassword(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("mustChangePassword", must);
        return ResponseEntity.ok(response);
        }
}