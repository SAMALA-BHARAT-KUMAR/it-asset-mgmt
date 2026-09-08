package com.itasset.assetservice.dto;

// API shape for a Location — id is null on the way in (DB generates it), set on the way out
public record LocationDto(
        Long id,
        String building,
        String floor,
        String room
) {
}
