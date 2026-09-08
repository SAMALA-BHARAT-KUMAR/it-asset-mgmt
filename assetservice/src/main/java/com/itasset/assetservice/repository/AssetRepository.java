package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // query derivation: Spring writes the SQL from the METHOD NAME — no @Query needed
    List<Asset> findByStatus(AssetStatus status);

    Optional<Asset> findBySerialNumber(String serialNumber);
}
