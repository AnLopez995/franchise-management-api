package com.franchise.management.domain.port;

import com.franchise.management.domain.model.Franchise;
import reactor.core.publisher.Mono;

/**
 * Persistence boundary for the {@link Franchise} aggregate. Implemented by the
 * infrastructure layer; the application service depends only on this interface.
 *
 * <p>The contract is reactive: operations return a {@link Mono} so the use cases compose
 * without blocking. Reactor Core is the single reactive abstraction the domain relies on;
 * it stays free of Spring and Mongo.
 */
public interface FranchiseRepositoryPort {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(String id);
}
