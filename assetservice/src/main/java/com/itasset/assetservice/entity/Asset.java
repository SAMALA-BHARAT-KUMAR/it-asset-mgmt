package com.itasset.assetservice.entity;

import com.itasset.assetservice.enums.AssetStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optimistic lock: concurrent assign/return on the same asset fail-fast (409) instead of
    // silently double-assigning. JPA reads/increments this; no setter (like id).
    @Version
    private Long version;

    @Column(unique = true) // backstops the service-layer existsBy check against a concurrent double-create
    private String assetTag;
    private String name;
    @Column(unique = true)
    private String serialNumber;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Location location;

    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    // JPA needs a no-arg constructor
    public Asset() {
    }

    public Long getId() {
        return id; // no setId — the database generates the id
    }

    public Long getVersion() {
        return version; // no setVersion — JPA manages it
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }
}
