package com.franchise.management.infrastructure.persistence;

import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexField;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end persistence test against a real MongoDB (Testcontainers) over the reactive driver.
 * Validates the aggregate round trip, optimistic locking and that the embedded-id indexes are
 * actually created. Skipped automatically when Docker is not available, so {@code mvn test} stays
 * green offline.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FranchisePersistenceIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        // Build the declared indexes on startup so this test can assert they exist.
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);
    }

    @Autowired
    private FranchiseRepositoryPort repository;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Test
    void persistsAndReadsBackTheWholeAggregate() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        Branch branch = Branch.create("Sucursal Centro");
        branch.addProduct(Product.create("Producto A", 50));
        franchise.addBranch(branch);

        Franchise saved = repository.save(franchise).block();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getVersion()).isEqualTo(0L); // first insert

        Franchise reloaded = repository.findById(saved.getId()).block();
        assertThat(reloaded).isNotNull();
        Branch reloadedBranch = reloaded.getBranches().get(0);
        assertThat(reloadedBranch.getName()).isEqualTo("Sucursal Centro");
        assertThat(reloadedBranch.getProducts().get(0).getStock()).isEqualTo(50);
    }

    @Test
    void versionIsBumpedOnEachUpdate() {
        Franchise saved = repository.save(Franchise.create("Franquicia A")).block();
        assertThat(saved).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);

        saved.rename("Franquicia A renombrada");
        Franchise updated = repository.save(saved).block();

        assertThat(updated).isNotNull();
        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void concurrentUpdateOverStaleAggregateFailsWithOptimisticLock() {
        Franchise saved = repository.save(Franchise.create("Franquicia Concurrente")).block();

        Franchise copyA = repository.findById(saved.getId()).block();
        Franchise copyB = repository.findById(saved.getId()).block();

        copyA.rename("Cambio A");
        repository.save(copyA).block(); // version 0 -> 1

        copyB.rename("Cambio B"); // still on stale version 0
        assertThatThrownBy(() -> repository.save(copyB).block())
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void embeddedIdIndexesAreCreated() {
        repository.save(Franchise.create("Indexada")).block(); // ensure collection exists

        List<String> indexedPaths = mongoTemplate.indexOps("franchises").getIndexInfo()
                .flatMapIterable(IndexInfo::getIndexFields)
                .map(IndexField::getKey)
                .collectList()
                .block();

        assertThat(indexedPaths).contains("branches._id", "branches.products._id");
    }
}
