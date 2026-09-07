package com.itasset.assetservice.service;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.repository.AssetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository repository;

    // constructor injection — no @Autowired field injection
    public AssetService(AssetRepository repository) {
        this.repository = repository;
    }

    // CREATE: save a new asset into the database and return it (now with a generated id)
    public Asset create(Asset asset) {
        return repository.save(asset);
    }

    // READ ALL: fetch every asset row from the database as a list
    public List<Asset> findAll() {
        return repository.findAll();
    }

    // READ ONE: find one asset by its id; if it doesn't exist, throw a 404 instead of crashing
    public Asset findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Asset not found: " + id));
    }

    // UPDATE: load the existing asset (404 if missing), copy the new values onto it, then save
    public Asset update(Long id, Asset changes) {
        Asset existing = findById(id); // reuses the 404 check above
        existing.setAssetTag(changes.getAssetTag());
        existing.setCategory(changes.getCategory());
        existing.setSerialNumber(changes.getSerialNumber());
        existing.setStatus(changes.getStatus());
        return repository.save(existing);
    }

    // DELETE: remove the asset by id; if it isn't there, throw a 404 first
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + id);
        }
        repository.deleteById(id);
    }
}
