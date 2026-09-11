package com.itasset.assetservice.enums;

import java.util.Set;

// Day 22: the asset lifecycle. ACTIVE = available/in stock, DEPLOYED = assigned to a user.
// Transitions are one-directional toward DISPOSED; return (DEPLOYED->ACTIVE) and repair
// (->ACTIVE) are the only "backward" moves the domain allows.
public enum AssetStatus {
    ACTIVE,      // available to assign (start state)
    DEPLOYED,    // currently assigned
    IN_REPAIR,
    RETIRED,
    DISPOSED;    // terminal

    private Set<AssetStatus> allowed() {
        return switch (this) {
            case ACTIVE    -> Set.of(DEPLOYED, IN_REPAIR, RETIRED);
            case DEPLOYED  -> Set.of(ACTIVE, IN_REPAIR, RETIRED); // ACTIVE = returned
            case IN_REPAIR -> Set.of(ACTIVE, RETIRED);
            case RETIRED   -> Set.of(DISPOSED);
            case DISPOSED  -> Set.of();
        };
    }

    public boolean canTransitionTo(AssetStatus target) {
        return allowed().contains(target);
    }
}
