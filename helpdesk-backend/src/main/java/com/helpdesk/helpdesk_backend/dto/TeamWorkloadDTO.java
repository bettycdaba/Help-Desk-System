package com.helpdesk.helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamWorkloadDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private long assignedCount;
    private long inProgressCount;
    private long pendingCount;
    private long resolvedCount;
    private long totalCount;
}