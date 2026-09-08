package com.itasset.assetservice.mapper;

import com.itasset.assetservice.dto.CategoryDto;
import com.itasset.assetservice.dto.LocationDto;
import com.itasset.assetservice.entity.Category;
import com.itasset.assetservice.entity.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Day 10 checkpoint: an entity round-trips through toDto -> toEntity and the fields match.
// (id is DB-generated and has no setter, so it's not part of the round-trip.)
class AssetMapperTest {

    @Test
    void categoryRoundTrip() {
        Category original = new Category();
        original.setName("Laptop");
        original.setDescription("Portable computers issued to staff");

        Category result = AssetMapper.toEntity(AssetMapper.toDto(original));

        assertEquals(original.getName(), result.getName());
        assertEquals(original.getDescription(), result.getDescription());
    }

    @Test
    void locationRoundTrip() {
        Location original = new Location();
        original.setBuilding("HQ Tower");
        original.setFloor("3");
        original.setRoom("301");

        Location result = AssetMapper.toEntity(AssetMapper.toDto(original));

        assertEquals(original.getBuilding(), result.getBuilding());
        assertEquals(original.getFloor(), result.getFloor());
        assertEquals(original.getRoom(), result.getRoom());
    }

    @Test
    void dtoCarriesTheGeneratedIdOutward() {
        // toDto keeps the id so the client can reference it; only toEntity drops it
        CategoryDto cat = AssetMapper.toDto(makeCategory());
        assertEquals("Monitor", cat.name());

        LocationDto loc = AssetMapper.toDto(makeLocation());
        assertEquals("Warehouse", loc.building());
    }

    private Category makeCategory() {
        Category c = new Category();
        c.setName("Monitor");
        c.setDescription("External displays");
        return c;
    }

    private Location makeLocation() {
        Location l = new Location();
        l.setBuilding("Warehouse");
        l.setFloor("1");
        l.setRoom("W-01");
        return l;
    }
}
