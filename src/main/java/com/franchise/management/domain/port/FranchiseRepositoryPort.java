package com.franchise.management.domain.port;

import com.franchise.management.domain.model.Franchise;

import java.util.Optional;

/**
 * Persistence boundary for the {@link Franchise} aggregate. Implemented by the
 * infrastructure layer; the application service depends only on this interface.
 */
public interface FranchiseRepositoryPort {

    Franchise save(Franchise franchise);

    Optional<Franchise> findById(String id);
}
