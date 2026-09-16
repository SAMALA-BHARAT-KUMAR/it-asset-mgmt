package com.itasset.assetservice.dto;

// Day 26: minimal view of an asset a person holds — just the identifying fields
public record AssetSummary(
        Long assignmentId, // the open assignment's id — what you POST to /{id}/return
        String serialNumber,
        String assetTag,
        String name
) {
}
