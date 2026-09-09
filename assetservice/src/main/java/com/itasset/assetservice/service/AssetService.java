package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.exception.DuplicateResourceException;
import com.itasset.assetservice.exception.ResourceNotFoundException;
import com.itasset.assetservice.repository.AssetRepository;
import com.itasset.assetservice.repository.AssetSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AssetService {

    private final AssetRepository repository;

    // constructor injection — no @Autowired field injection
    public AssetService(AssetRepository repository) {
        this.repository = repository;
    }

    // CREATE: reject duplicates on unique fields, then save and return (now with a generated id)
    public Asset create(Asset asset) {
        if (repository.existsByAssetTag(asset.getAssetTag())) {
            throw new DuplicateResourceException("Asset tag already exists: " + asset.getAssetTag());
        }
        if (repository.existsBySerialNumber(asset.getSerialNumber())) {
            throw new DuplicateResourceException("Serial number already exists: " + asset.getSerialNumber());
        }
        return repository.save(asset);
    }

    // READ ALL: fetch every asset row from the database as a list
    public List<Asset> findAll() {
        return repository.findAll();
    }

    // SEARCH: optional status/category filters + paging + sorting, all in one query
    public Page<Asset> search(AssetStatus status, Long categoryId, Pageable pageable) {
        return repository.findAll(AssetSpecifications.filter(status, categoryId), pageable);
    }

    // STATS: per-category counts via ONE GROUP BY query (no N+1, no loading all rows into memory)
    public Map<String, Long> countByCategory() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : repository.countGroupedByCategory()) {
            String name = row[0] == null ? "(uncategorized)" : (String) row[0];
            counts.put(name, (Long) row[1]);
        }
        return counts;
    }

    // READ ONE: find one asset by its id; if it doesn't exist, throw a 404 instead of crashing
    public Asset findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    // UPDATE: load the existing asset (404 if missing), copy the new values onto it, then save
    public Asset update(Long id, Asset changes) {
        Asset existing = findById(id); // reuses the 404 check above
        // only guard when the unique value actually changed, else it collides with itself
        if (!Objects.equals(existing.getAssetTag(), changes.getAssetTag())
                && repository.existsByAssetTag(changes.getAssetTag())) {
            throw new DuplicateResourceException("Asset tag already exists: " + changes.getAssetTag());
        }
        if (!Objects.equals(existing.getSerialNumber(), changes.getSerialNumber())
                && repository.existsBySerialNumber(changes.getSerialNumber())) {
            throw new DuplicateResourceException("Serial number already exists: " + changes.getSerialNumber());
        }
        existing.setAssetTag(changes.getAssetTag());
        existing.setName(changes.getName());
        existing.setCategory(changes.getCategory());
        existing.setLocation(changes.getLocation());
        existing.setSerialNumber(changes.getSerialNumber());
        existing.setStatus(changes.getStatus());
        return repository.save(existing);
    }

    // DELETE: remove the asset by id; if it isn't there, throw a 404 first
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Asset not found: " + id);
        }
        repository.deleteById(id);
    }
}
