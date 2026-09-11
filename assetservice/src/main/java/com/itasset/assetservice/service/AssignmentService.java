package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Assignment;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.exception.AssetNotAvailableException;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.AssignmentRepository;
import com.itasset.assetservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AssignmentService {

    private final AssignmentRepository assignments;
    private final AssetRepository assets;
    private final UserRepository users;

    public AssignmentService(AssignmentRepository assignments, AssetRepository assets, UserRepository users) {
        this.assignments = assignments;
        this.assets = assets;
        this.users = users;
    }

    // Day 23: hand an asset to a user. Two writes (new assignment row + asset status flip)
    // in ONE @Transactional unit — either both land or neither does, no half-assigned state.
    @Transactional
    public Assignment assignAsset(Long assetId, Long userId, String notes, String assignedBy) {
        Asset asset = assets.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
        User user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // already out on an open assignment?
        if (assignments.existsByAssetIdAndReturnedAtIsNull(assetId)) {
            throw new AssetNotAvailableException("Asset is already assigned: " + assetId);
        }
        // in a status that can't be deployed (IN_REPAIR / RETIRED / DISPOSED)?
        if (!asset.getStatus().canTransitionTo(AssetStatus.DEPLOYED)) {
            throw new AssetNotAvailableException(
                    "Asset cannot be assigned from status " + asset.getStatus());
        }

        Assignment assignment = new Assignment();
        assignment.setAsset(asset);
        assignment.setUser(user);
        assignment.setAssignedAt(Instant.now());
        assignment.setAssignedBy(assignedBy);
        assignment.setNotes(notes);

        asset.setStatus(AssetStatus.DEPLOYED);
        assets.save(asset);
        return assignments.save(assignment);
    }
}
