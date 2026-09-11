package com.itasset.assetservice.controller;

import com.itasset.assetservice.dto.AssignmentResponse;
import com.itasset.assetservice.dto.EmployeeAssetsResponse;
import com.itasset.assetservice.service.AssignmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Day 25-26: read-only views over assignments. Any authenticated user may read; no @PreAuthorize.
// Full (non-/api/assignments) paths, so this is its own controller rather than AssignmentController.
@RestController
public class AssignmentQueryController {

    private final AssignmentService service;

    public AssignmentQueryController(AssignmentService service) {
        this.service = service;
    }

    // Day 25: history of one asset, newest-first
    @GetMapping("/api/assets/{id}/assignments")
    public List<AssignmentResponse> assetHistory(@PathVariable Long id) {
        return service.historyForAsset(id).stream().map(AssignmentResponse::from).toList();
    }

    // Day 25: history of one user, newest-first
    @GetMapping("/api/users/{id}/assignments")
    public List<AssignmentResponse> userHistory(@PathVariable Long id) {
        return service.historyForUser(id).stream().map(AssignmentResponse::from).toList();
    }

    // Day 26: the flagship — what this employee currently holds
    @GetMapping("/api/employees/{id}/assets")
    public EmployeeAssetsResponse employeeAssets(@PathVariable Long id) {
        return service.employeeAssets(id);
    }
}
