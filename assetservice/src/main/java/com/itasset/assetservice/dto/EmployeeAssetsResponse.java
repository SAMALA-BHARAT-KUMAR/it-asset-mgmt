package com.itasset.assetservice.dto;

import java.util.List;

// Day 26: the flagship answer — "what does this employee currently have?"
public record EmployeeAssetsResponse(
        String employeeName,
        List<AssetSummary> assets,
        int count
) {
}
