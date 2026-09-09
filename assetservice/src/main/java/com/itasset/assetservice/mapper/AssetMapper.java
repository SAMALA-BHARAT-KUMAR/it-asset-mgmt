package com.itasset.assetservice.mapper;

import com.itasset.assetservice.dto.AssetRequest;
import com.itasset.assetservice.dto.AssetResponse;
import com.itasset.assetservice.dto.CategoryDto;
import com.itasset.assetservice.dto.LocationDto;
import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Category;
import com.itasset.assetservice.entity.Location;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.CategoryRepository;
import com.itasset.assetservice.repository.LocationRepository;
import org.springframework.stereotype.Component;

// translates between the API shapes (DTOs) and the database shape (Asset entity)
@Component
public class AssetMapper {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public AssetMapper(CategoryRepository categoryRepository, LocationRepository locationRepository) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    // incoming request DTO -> fresh Asset entity (no id — the DB generates it)
    public Asset toEntity(AssetRequest request) {
        Asset asset = new Asset();
        asset.setAssetTag(request.assetTag());
        asset.setName(request.name());
        asset.setSerialNumber(request.serialNumber());
        asset.setStatus(request.status());
        asset.setCategory(loadCategory(request.categoryId()));
        asset.setLocation(loadLocation(request.locationId())); // null if none given
        return asset;
    }

    // Asset entity -> response DTO the client receives
    public AssetResponse toResponse(Asset asset) {
        Category category = asset.getCategory();
        Location location = asset.getLocation();
        return new AssetResponse(
                asset.getId(),
                asset.getAssetTag(),
                asset.getName(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                location == null ? null : location.getId(),
                location == null ? null : label(location),
                asset.getSerialNumber(),
                asset.getStatus()
        );
    }

    // look up the referenced category; 404 if the client sent an id that doesn't exist
    private Category loadCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    // location is optional: null id -> no location; a given id must exist or it's a 404
    private Location loadLocation(Long id) {
        if (id == null) {
            return null;
        }
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
    }

    private String label(Location l) {
        return l.getBuilding() + " / " + l.getFloor() + " / " + l.getRoom();
    }

    // --- pure Category/Location <-> DTO mapping (static: no DB lookups needed) ---

    public static CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getDescription());
    }

    // id is DB-generated, so a DTO -> entity conversion never carries it back in
    public static Category toEntity(CategoryDto dto) {
        Category c = new Category();
        c.setName(dto.name());
        c.setDescription(dto.description());
        return c;
    }

    public static LocationDto toDto(Location l) {
        return new LocationDto(l.getId(), l.getBuilding(), l.getFloor(), l.getRoom());
    }

    public static Location toEntity(LocationDto dto) {
        Location l = new Location();
        l.setBuilding(dto.building());
        l.setFloor(dto.floor());
        l.setRoom(dto.room());
        return l;
    }
}
