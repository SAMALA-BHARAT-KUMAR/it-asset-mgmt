package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AssetRequest;
import com.itasset.assetservice.dto.AssetResponse;
import com.itasset.assetservice.enums.AssetStatus;
import com.itasset.assetservice.mapper.AssetMapper;
import com.itasset.assetservice.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assets") // all endpoints below start with /api/assets
public class AssetController {

    private final AssetService service;
    private final AssetMapper mapper;

    // constructor injection — Spring hands us the service and the mapper
    public AssetController(AssetService service, AssetMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // CREATE: POST /api/assets  → 201 Created, returns the saved asset (with generated id)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        return mapper.toResponse(service.create(mapper.toEntity(request)));
    }

    // READ ALL: GET /api/assets?status=&categoryId=&page=&size=&sort=field,dir
    // optional filters + paging + sorting; returns a Page (content + total counts)
    @GetMapping
    public Page<AssetResponse> findAll(
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        return service.search(status, categoryId, pageable).map(mapper::toResponse);
    }

    // STATS: GET /api/assets/stats/by-category  → { "Laptop": 5, "Monitor": 3, ... }
    @GetMapping("/stats/by-category")
    public Map<String, Long> statsByCategory() {
        return service.countByCategory();
    }

    // READ ONE: GET /api/assets/{id}  → 200 OK, or 404 if the id doesn't exist
    @GetMapping("/{id}")
    public AssetResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    // UPDATE: PUT /api/assets/{id}  → 200 OK with the updated asset, or 404
    @PutMapping("/{id}")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return mapper.toResponse(service.update(id, mapper.toEntity(request)));
    }

    // DELETE: DELETE /api/assets/{id}  → 204 No Content, or 404
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
