package com.itasset.assetservice.mapper;

import com.itasset.assetservice.dto.AssetRequest;
import com.itasset.assetservice.dto.AssetResponse;
import com.itasset.assetservice.entity.Asset;
import org.springframework.stereotype.Component;

// translates between the API shapes (DTOs) and the database shape (Asset entity)
@Component
public class AssetMapper {

    // incoming request DTO -> fresh Asset entity (no id — the DB generates it)
    public Asset toEntity(AssetRequest request) {
        Asset asset = new Asset();
        asset.setAssetTag(request.assetTag());
        asset.setCategory(request.category());
        asset.setSerialNumber(request.serialNumber());
        asset.setStatus(request.status());
        return asset;
    }

    // Asset entity -> response DTO the client receives
    public AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetTag(),
                asset.getCategory(),
                asset.getSerialNumber(),
                asset.getStatus()
        );
    }
}
