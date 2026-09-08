package com.itasset.assetservice.mapper;

import com.itasset.assetservice.dto.AssetRequest;
import com.itasset.assetservice.dto.AssetResponse;
import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Category;
import com.itasset.assetservice.entity.Location;
import com.itasset.assetservice.repository.CategoryRepository;
import com.itasset.assetservice.repository.LocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found: " + id));
    }

    // location is optional: null id -> no location; a given id must exist or it's a 404
    private Location loadLocation(Long id) {
        if (id == null) {
            return null;
        }
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Location not found: " + id));
    }

    private String label(Location l) {
        return l.getBuilding() + " / " + l.getFloor() + " / " + l.getRoom();
    }
}
