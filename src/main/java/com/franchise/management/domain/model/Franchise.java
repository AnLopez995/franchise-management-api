package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.BranchNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root. A franchise owns its branches (and, transitively, their products)
 * and is the single unit loaded, mutated and persisted by the application service.
 */
public class Franchise {

    private final String id;
    private String name;
    private final List<Branch> branches;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Long version;

    private Franchise(String id, String name, List<Branch> branches,
                      LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.name = name;
        this.branches = branches;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Factory for a brand-new franchise; no id/version yet (assigned on persistence), empty branches. */
    public static Franchise create(String name) {
        LocalDateTime now = LocalDateTime.now();
        return new Franchise(null, name, new ArrayList<>(), now, now, null);
    }

    /**
     * Rehydrates an existing franchise (e.g. from persistence) keeping its id, timestamps and
     * optimistic-locking {@code version}.
     */
    public static Franchise rehydrate(String id, String name, List<Branch> branches,
                                      LocalDateTime createdAt, LocalDateTime updatedAt, Long version) {
        return new Franchise(id, name, new ArrayList<>(branches), createdAt, updatedAt, version);
    }

    public void addBranch(Branch branch) {
        branches.add(branch);
        touch();
    }

    public Branch getBranch(String branchId) {
        return branches.stream()
                .filter(branch -> branch.getId().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    public void rename(String newName) {
        this.name = newName;
        touch();
    }

    /** Advances {@code updatedAt}; call after mutating any nested branch/product. */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Read-only view of the branches held by this franchise. */
    public List<Branch> getBranches() {
        return List.copyOf(branches);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Optimistic-locking version; {@code null} for a not-yet-persisted aggregate. */
    public Long getVersion() {
        return version;
    }
}
