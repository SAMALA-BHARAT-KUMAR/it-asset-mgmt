package com.itasset.assetservice.dto;

import jakarta.validation.constraints.NotNull;

// Day 23: body of POST /api/assignments — who gets which asset (notes optional)
public record AssignRequest(
        @NotNull Long assetId,
        @NotNull Long userId,
        String notes
) {
}
