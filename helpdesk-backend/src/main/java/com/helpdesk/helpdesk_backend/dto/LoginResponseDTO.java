package com.helpdesk.helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String message;
    private List<String> roles;
    private List<String> permissions;
}