package com.itasset.assetservice.controller;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets") // all endpoints below start with /api/assets
public class AssetController {

    private final AssetService service;

    // constructor injection — Spring hands us the AssetService
    public AssetController(AssetService service) {
        this.service = service;
    }

    // CREATE: POST /api/assets  → 201 Created, returns the saved asset (with generated id)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asset create(@RequestBody Asset asset) {
        return service.create(asset);
    }

    // READ ALL: GET /api/assets  → 200 OK, list of every asset
    @GetMapping
    public List<Asset> findAll() {
        return service.findAll();
    }

    // READ ONE: GET /api/assets/{id}  → 200 OK, or 404 if the id doesn't exist
    @GetMapping("/{id}")
    public Asset findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // UPDATE: PUT /api/assets/{id}  → 200 OK with the updated asset, or 404
    @PutMapping("/{id}")
    public Asset update(@PathVariable Long id, @RequestBody Asset changes) {
        return service.update(id, changes);
    }

    // DELETE: DELETE /api/assets/{id}  → 204 No Content, or 404
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
