package com.itasset.assetservice.repository;

import com.itasset.assetservice.entity.Asset;
import com.itasset.assetservice.enums.AssetStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// builds the query's WHERE clause piece by piece: each filter is added ONLY if it was supplied.
// one clean path handles all filter combinations (none / status / category / both).
public final class AssetSpecifications {

    private AssetSpecifications() {
    }

    public static Specification<Asset> filter(AssetStatus status, Long categoryId) {
        return (root, query, cb) -> {
            // N+1 fix: pull category + location in the SAME query via LEFT JOIN FETCH.
            // skip on the COUNT query (Long result) — you can't fetch-join a count.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("location", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            // empty list -> cb.and() is always-true -> returns everything
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
