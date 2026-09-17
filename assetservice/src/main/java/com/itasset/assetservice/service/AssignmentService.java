package com.itasset.assetservice.service;

import com.itasset.assetservice.dto.AssetSummary;
import com.itasset.assetservice.dto.EmployeeAssetsResponse;
import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Assignment;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.exception.AssetNotAvailableException;
import com.itasset.assetservice.exception.InvalidAssignmentStateException;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignments;
    private final AssetRepository assets;

    public AssignmentService(AssignmentRepository assignments, AssetRepository assets) {
        this.assignments = assignments;
        this.assets = assets;
    }

    // Day 23: hand an asset to a user. Two writes (new assignment row + asset status flip)
    // in ONE @Transactional unit — either both land or neither does, no half-assigned state.
    @Transactional
    public Assignment assignAsset(Long assetId, Long userId, String notes, String assignedBy) {
        Asset asset = assets.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
        if (userId == null) {
            throw new ResourceNotFoundException("userId is required");
        }
        // ponytail: userId not validated against user-auth-service here; verify via inter-service
        // call (or gateway-propagated identity) when that wiring lands (later day).

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
        assignment.setUserId(userId);
        assignment.setAssignedAt(Instant.now());
        assignment.setAssignedBy(assignedBy);
        assignment.setNotes(notes);

        asset.setStatus(AssetStatus.DEPLOYED);
        assets.save(asset);
        return assignments.save(assignment);
    }

    // Day 24: close an open assignment — stamp returnedAt and flip the asset back to ACTIVE.
    // Both writes in ONE @Transactional, same all-or-nothing guarantee as assign.
    @Transactional
    public Assignment returnAsset(Long assignmentId) {
        Assignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getReturnedAt() != null) {
            throw new InvalidAssignmentStateException("Assignment already returned: " + assignmentId);
        }

        assignment.setReturnedAt(Instant.now());
        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.ACTIVE); // back in stock, available to assign again
        assets.save(asset);
        return assignments.save(assignment);
    }

    // Day 25: full history for one asset / one user, newest-first
    public List<Assignment> historyForAsset(Long assetId) {
        return assignments.findByAssetIdOrderByAssignedAtDesc(assetId);
    }

    public List<Assignment> historyForUser(Long userId) {
        return assignments.findByUserIdOrderByAssignedAtDesc(userId);
    }

    // Day 26: the flagship query — what does this employee currently hold?
    public EmployeeAssetsResponse employeeAssets(Long userId) {
        List<AssetSummary> held = assignments.findByUserIdAndReturnedAtIsNull(userId).stream()
                .map(assignment -> {
                    Asset a = assignment.getAsset();
                    return new AssetSummary(assignment.getId(), a.getSerialNumber(), a.getAssetTag(), a.getName());
                })
                .toList();
        // ponytail: real employee name lives in user-auth-service; fetch it via inter-service call
        // (or gateway-propagated identity) later. For now surface the id so the endpoint still works.
        return new EmployeeAssetsResponse("user #" + userId, held, held.size());
    }
}
