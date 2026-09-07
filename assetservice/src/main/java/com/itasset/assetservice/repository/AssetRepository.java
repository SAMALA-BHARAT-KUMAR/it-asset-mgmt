package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}
