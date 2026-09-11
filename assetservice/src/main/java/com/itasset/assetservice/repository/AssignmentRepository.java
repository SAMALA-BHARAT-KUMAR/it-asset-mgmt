package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // is this asset currently out? (an open assignment = returnedAt IS NULL)
    boolean existsByAssetIdAndReturnedAtIsNull(Long assetId);

    // Day 25: full history, newest-first
    List<Assignment> findByAssetIdOrderByAssignedAtDesc(Long assetId);

    List<Assignment> findByUserIdOrderByAssignedAtDesc(Long userId);

    // Day 26: what this user currently holds (open assignments only)
    List<Assignment> findByUserIdAndReturnedAtIsNull(Long userId);
}
