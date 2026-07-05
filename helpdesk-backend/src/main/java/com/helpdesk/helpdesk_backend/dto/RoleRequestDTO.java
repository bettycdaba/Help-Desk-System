package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDTO {

    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}