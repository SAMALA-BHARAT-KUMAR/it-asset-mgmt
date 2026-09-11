package com.itasset.assetservice.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Day 22 checkpoint: the lifecycle only moves forward toward DISPOSED (plus return/repair
// back to ACTIVE). No resurrecting a RETIRED/DISPOSED asset.
class AssetStatusTest {

    @Test
    void legalTransitions() {
        assertTrue(AssetStatus.ACTIVE.canTransitionTo(AssetStatus.DEPLOYED));   // assign
        assertTrue(AssetStatus.DEPLOYED.canTransitionTo(AssetStatus.ACTIVE));   // return
        assertTrue(AssetStatus.IN_REPAIR.canTransitionTo(AssetStatus.ACTIVE));  // fixed
        assertTrue(AssetStatus.RETIRED.canTransitionTo(AssetStatus.DISPOSED));
    }

    @Test
    void illegalTransitions() {
        assertFalse(AssetStatus.RETIRED.canTransitionTo(AssetStatus.DEPLOYED)); // no backward
        assertFalse(AssetStatus.DISPOSED.canTransitionTo(AssetStatus.ACTIVE));  // terminal
        assertFalse(AssetStatus.ACTIVE.canTransitionTo(AssetStatus.DISPOSED));  // must retire first
    }
}
