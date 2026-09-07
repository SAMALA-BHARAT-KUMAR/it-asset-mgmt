package com.itasset.assetservice.dto;

import com.itasset.assetservice.enums.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        @NotBlank String assetTag,
        String category,
        @NotBlank String serialNumber,
        @NotNull AssetStatus status
) {
}
