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
public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private Set<Long> supervisorIds;
    private Set<String> supervisorNames;
}