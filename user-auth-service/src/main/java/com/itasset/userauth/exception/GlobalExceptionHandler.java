package com.itasset.userauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Validation failures + duplicate user/email -> clean JSON. (Auth flows use ResponseStatusException
// directly for 401s, so no handler needed for those.)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleDuplicate(DuplicateResourceException ex) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        error.put("message", ex.getMessage());
        return error;
    }
}
