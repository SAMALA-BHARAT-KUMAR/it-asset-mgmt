package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.exception.DuplicateResourceException;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Day 14: unit-test the service in isolation — repository is mocked, so no DB is touched.
// covers create / get / update / delete plus the not-found and duplicate paths.
@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository repository;

    @InjectMocks
    private AssetService service;

    private Asset sample() {
        Asset a = new Asset();
        a.setAssetTag("LAP-001");
        a.setName("Dev laptop");
        a.setSerialNumber("SN001");
        a.setStatus(AssetStatus.ACTIVE);
        return a;
    }

    // --- create ---

    @Test
    void create_savesWhenNoDuplicate() {
        Asset a = sample();
        when(repository.existsByAssetTag("LAP-001")).thenReturn(false);
        when(repository.existsBySerialNumber("SN001")).thenReturn(false);
        when(repository.save(a)).thenReturn(a);

        assertSame(a, service.create(a));
        verify(repository).save(a);
    }

    @Test
    void create_rejectsDuplicateAssetTag() {
        Asset a = sample();
        when(repository.existsByAssetTag("LAP-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(a));
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsDuplicateSerialNumber() {
        Asset a = sample();
        when(repository.existsByAssetTag("LAP-001")).thenReturn(false);
        when(repository.existsBySerialNumber("SN001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(a));
        verify(repository, never()).save(any());
    }

    // --- get ---

    @Test
    void findById_returnsAssetWhenPresent() {
        Asset a = sample();
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        assertSame(a, service.findById(1L));
    }

    @Test
    void findById_throwsWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    // --- update ---

    @Test
    void update_copiesChangesAndSaves() {
        Asset existing = sample();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Asset changes = new Asset();
        changes.setAssetTag("LAP-001");     // same tag/serial -> dup check short-circuits
        changes.setSerialNumber("SN001");
        changes.setName("Renamed laptop");
        changes.setStatus(AssetStatus.IN_REPAIR);

        Asset result = service.update(1L, changes);

        assertEquals("Renamed laptop", result.getName());
        assertEquals(AssetStatus.IN_REPAIR, result.getStatus());
        verify(repository).save(existing);
    }

    @Test
    void update_throwsWhenMissing() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(999L, sample()));
        verify(repository, never()).save(any());
    }

    // --- delete ---

    @Test
    void delete_removesWhenPresent() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(999L));
        verify(repository, never()).deleteById(any());
    }
}
