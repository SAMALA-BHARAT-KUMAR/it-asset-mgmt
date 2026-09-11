package com.itasset.assetservice.dto;

// Day 26: minimal view of an asset a person holds — just the identifying fields
public record AssetSummary(
        String serialNumber,
        String assetTag,
        String name
) {
}
