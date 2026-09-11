package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AssignRequest;
import com.itasset.assetservice.dto.AssignmentResponse;
import com.itasset.assetservice.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService service;

    public AssignmentController(AssignmentService service) {
        this.service = service;
    }

    // ASSIGN: POST /api/assignments → 201 Created. ADMIN only (EMPLOYEE gets 403).
    // principal = the logged-in user; we record their username as assignedBy.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse assign(@Valid @RequestBody AssignRequest request, Principal principal) {
        return AssignmentResponse.from(
                service.assignAsset(request.assetId(), request.userId(), request.notes(), principal.getName()));
    }

    // RETURN: POST /api/assignments/{id}/return → 200 OK. ADMIN only.
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public AssignmentResponse returnAsset(@PathVariable Long id) {
        return AssignmentResponse.from(service.returnAsset(id));
    }
}
