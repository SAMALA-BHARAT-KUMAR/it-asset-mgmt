package com.itasset.assetservice.exception;

// Day 23: thrown when you try to assign an asset that's already out, or in a status
// that can't be deployed (IN_REPAIR / RETIRED / DISPOSED) -> mapped to 409 Conflict.
public class AssetNotAvailableException extends RuntimeException {
    public AssetNotAvailableException(String message) {
        super(message);
    }
}
