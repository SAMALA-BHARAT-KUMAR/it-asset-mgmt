package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.entity.Assignment;
import com.itasset.assetservice.entity.User;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.exception.AssetNotAvailableException;
import com.itasset.assetservice.exception.InvalidAssignmentStateException;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.AssignmentRepository;
import com.itasset.assetservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Day 23: assign flow in isolation — repos mocked, no DB. Covers success, already-assigned,
// non-deployable status, and missing asset/user.
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock private AssignmentRepository assignments;
    @Mock private AssetRepository assets;
    @Mock private UserRepository users;

    @InjectMocks private AssignmentService service;

    private Asset asset(AssetStatus status) {
        Asset a = new Asset();
        a.setAssetTag("LAP-001");
        a.setStatus(status);
        return a;
    }

    private User user() {
        User u = new User();
        u.setFullName("Bharat");
        return u;
    }

    @Test
    void assign_success_flipsStatusAndSaves() {
        when(assets.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.ACTIVE)));
        when(users.findById(2L)).thenReturn(Optional.of(user()));
        when(assignments.existsByAssetIdAndReturnedAtIsNull(1L)).thenReturn(false);
        when(assignments.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment result = service.assignAsset(1L, 2L, "new hire", "admin");

        assertEquals(AssetStatus.DEPLOYED, result.getAsset().getStatus());
        assertEquals("admin", result.getAssignedBy());
        assertNull(result.getReturnedAt()); // still out
        verify(assets).save(any(Asset.class)); // status flip persisted
    }

    @Test
    void assign_alreadyOut_throws409() {
        when(assets.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.ACTIVE)));
        when(users.findById(2L)).thenReturn(Optional.of(user()));
        when(assignments.existsByAssetIdAndReturnedAtIsNull(1L)).thenReturn(true);

        assertThrows(AssetNotAvailableException.class,
                () -> service.assignAsset(1L, 2L, null, "admin"));
        verify(assignments, never()).save(any());
    }

    @Test
    void assign_retiredAsset_throws409() {
        when(assets.findById(1L)).thenReturn(Optional.of(asset(AssetStatus.RETIRED)));
        when(users.findById(2L)).thenReturn(Optional.of(user()));
        when(assignments.existsByAssetIdAndReturnedAtIsNull(1L)).thenReturn(false);

        assertThrows(AssetNotAvailableException.class,
                () -> service.assignAsset(1L, 2L, null, "admin"));
    }

    @Test
    void assign_missingAsset_throws404() {
        when(assets.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.assignAsset(1L, 2L, null, "admin"));
    }

    // --- return ---

    private Assignment openAssignment() {
        Assignment a = new Assignment();
        a.setAsset(asset(AssetStatus.DEPLOYED));
        a.setUser(user());
        a.setAssignedAt(Instant.now());
        return a; // returnedAt == null -> still out
    }

    @Test
    void return_success_stampsReturnedAtAndFreesAsset() {
        Assignment open = openAssignment();
        when(assignments.findById(5L)).thenReturn(Optional.of(open));
        when(assignments.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

        Assignment result = service.returnAsset(5L);

        assertNotNull(result.getReturnedAt()); // now closed
        assertEquals(AssetStatus.ACTIVE, result.getAsset().getStatus()); // back in stock
        verify(assets).save(any(Asset.class));
    }

    @Test
    void return_alreadyReturned_throws400() {
        Assignment closed = openAssignment();
        closed.setReturnedAt(Instant.now());
        when(assignments.findById(5L)).thenReturn(Optional.of(closed));

        assertThrows(InvalidAssignmentStateException.class, () -> service.returnAsset(5L));
        verify(assets, never()).save(any());
    }

    @Test
    void return_missingAssignment_throws404() {
        when(assignments.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.returnAsset(5L));
    }
}
