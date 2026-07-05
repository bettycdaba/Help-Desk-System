package com.helpdesk.helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Boolean active;
    private Long departmentId;
    private String departmentName;
    private Set<Long> roleIds;
    private Set<String> roleNames;
}