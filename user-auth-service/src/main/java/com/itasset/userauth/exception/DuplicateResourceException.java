package com.itasset.userauth.exception;

// thrown when a unique field (username / email) already exists -> mapped to 409 Conflict
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
