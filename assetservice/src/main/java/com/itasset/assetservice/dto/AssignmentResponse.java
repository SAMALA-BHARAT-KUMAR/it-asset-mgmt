package com.itasset.assetservice.dto;

import com.itasset.assetservice.entity.Assignment;

import java.time.Instant;

// what the API sends back for an assignment — flattened, no entities on the wire
public record AssignmentResponse(
        Long id,
        Long assetId,
        String assetTag,
        Long userId,
        String userName,
        Instant assignedAt,
        Instant returnedAt, // null = still out
        String assignedBy,
        String notes
) {
    public static AssignmentResponse from(Assignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getAsset().getId(),
                a.getAsset().getAssetTag(),
                a.getUserId(),
                null, // ponytail: userName lives in user-auth-service now; resolve via Feign on Day 39
                a.getAssignedAt(),
                a.getReturnedAt(),
                a.getAssignedBy(),
                a.getNotes()
        );
    }
}
