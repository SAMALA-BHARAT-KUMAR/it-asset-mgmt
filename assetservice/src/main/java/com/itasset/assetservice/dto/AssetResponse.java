package com.itasset.assetservice.dto;

import com.itasset.assetservice.enums.AssetStatus;

// what the API sends BACK to the client — only safe fields, includes the generated id
public record AssetResponse(
        Long id,
        String assetTag,
        String name,
        Long categoryId,
        String categoryName,
        Long locationId,
        String locationLabel, // "building / floor / room", or null if unplaced
        String serialNumber,
        AssetStatus status
) {
}
