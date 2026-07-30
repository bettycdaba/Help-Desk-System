package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String temporaryPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}