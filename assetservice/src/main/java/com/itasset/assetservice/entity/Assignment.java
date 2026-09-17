package com.itasset.assetservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

// Day 22: who has which asset, and when. returnedAt == null means the assignment is still open.
// extends Auditable: createdBy = who assigned, updatedBy = who returned (auto-filled)
@Entity
@Table(name = "assignments")
public class Assignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Asset asset;

    // Day 31/32: user lives in user-auth-service now. Store only the id — no cross-DB FK/join.
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant assignedAt;

    private Instant returnedAt; // null while the asset is still out

    private String assignedBy; // username from SecurityContext

    private String notes;

    // JPA needs a no-arg constructor
    public Assignment() {
    }

    public Long getId() {
        return id; // no setId — the database generates it
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Instant returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
