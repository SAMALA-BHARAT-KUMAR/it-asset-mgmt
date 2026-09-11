package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // is this asset currently out? (an open assignment = returnedAt IS NULL)
    boolean existsByAssetIdAndReturnedAtIsNull(Long assetId);
}
