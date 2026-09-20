package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    @Email(message = "Email must be a valid email address")
    @Pattern(regexp = "^[A-Za-z0-9](?:[A-Za-z0-9._%+-]*[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$", message = "Email must be a valid email address")
    private String email;

    @NotBlank
    private String temporaryPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}