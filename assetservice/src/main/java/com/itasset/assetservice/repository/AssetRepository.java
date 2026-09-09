package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor adds findAll(Specification, Pageable) for dynamic filters + paging
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    // query derivation: Spring writes the SQL from the METHOD NAME — no @Query needed
    List<Asset> findByStatus(AssetStatus status);

    Optional<Asset> findBySerialNumber(String serialNumber);

    // duplicate checks for create/update — Spring writes the SQL from the method name
    boolean existsByAssetTag(String assetTag);

    boolean existsBySerialNumber(String serialNumber);

    // stats without N+1: let the DB count per category in ONE query.
    // LEFT JOIN keeps assets whose category is null (they group under a null name).
    // each row = [categoryName, count]
    @Query("SELECT c.name, COUNT(a) FROM Asset a LEFT JOIN a.category c GROUP BY c.name")
    List<Object[]> countGroupedByCategory();
}
