package com.itasset.assetservice.dto;

import com.itasset.assetservice.enums.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        @NotBlank String assetTag,
        String name, // optional — human-friendly label
        @NotNull Long categoryId,
        Long locationId, // optional — an asset may not be placed anywhere yet
        @NotBlank String serialNumber,
        @NotNull AssetStatus status
) {
}
