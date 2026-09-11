package com.itasset.assetservice.exception;

// Day 24: thrown when an assignment action doesn't fit its current state
// (e.g. returning an already-returned assignment) -> mapped to 400 Bad Request.
public class InvalidAssignmentStateException extends RuntimeException {
    public InvalidAssignmentStateException(String message) {
        super(message);
    }
}
