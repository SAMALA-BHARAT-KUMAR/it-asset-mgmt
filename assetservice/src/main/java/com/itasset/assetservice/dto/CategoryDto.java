package com.itasset.assetservice.dto;

// API shape for a Category — id is null on the way in (DB generates it), set on the way out
public record CategoryDto(
        Long id,
        String name,
        String description
) {
}
