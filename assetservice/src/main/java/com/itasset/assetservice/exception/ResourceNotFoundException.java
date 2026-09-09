package com.itasset.assetservice.exception;

// thrown when an asset id doesn't exist -> mapped to 404 by GlobalExceptionHandler
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
