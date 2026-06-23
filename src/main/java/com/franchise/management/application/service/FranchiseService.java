package com.franchise.management.application.service;

import com.franchise.management.domain.exception.FranchiseNotFoundException;
import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import com.franchise.management.domain.port.FranchiseRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Application service orchestrating the franchise use cases. Loads the {@link Franchise}
 * aggregate through the port, applies the requested change on the domain model, refreshes
 * {@code updatedAt} and persists the result. No business rules live here beyond orchestration.
 *
 * <p>The flow is reactive end to end: domain invariants thrown inside the mapping functions
 * (negative stock, missing branch/product) surface as {@code onError} signals, and a missing
 * franchise becomes a {@link FranchiseNotFoundException} via {@code switchIfEmpty}.
 */
@Service
public class FranchiseService {

    private final FranchiseRepositoryPort repository;

    public FranchiseService(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Mono<Franchise> createFranchise(String name) {
        return repository.save(Franchise.create(name));
    }

    public Mono<Branch> addBranch(String franchiseId, String branchName) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            Branch branch = Branch.create(branchName);
            franchise.addBranch(branch);
            return repository.save(franchise).map(saved -> saved.getBranch(branch.getId()));
        });
    }

    public Mono<Product> addProduct(String franchiseId, String branchId, String productName, int stock) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            Branch branch = franchise.getBranch(branchId);
            Product product = Product.create(productName, stock);
            branch.addProduct(product);
            franchise.touch();
            return repository.save(franchise)
                    .map(saved -> saved.getBranch(branchId).getProduct(product.getId()));
        });
    }

    public Mono<Void> removeProduct(String franchiseId, String branchId, String productId) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            franchise.getBranch(branchId).removeProduct(productId);
            franchise.touch();
            return repository.save(franchise);
        }).then();
    }

    public Mono<Product> updateStock(String franchiseId, String branchId, String productId, int stock) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            franchise.getBranch(branchId).getProduct(productId).changeStock(stock);
            franchise.touch();
            return repository.save(franchise)
                    .map(saved -> saved.getBranch(branchId).getProduct(productId));
        });
    }

    public Flux<TopStockProduct> getTopStockProducts(String franchiseId) {
        return loadFranchise(franchiseId).flatMapMany(franchise -> Flux.fromStream(
                franchise.getBranches().stream()
                        .map(branch -> branch.topStockProduct()
                                .map(product -> TopStockProduct.of(branch, product)))
                        .flatMap(Optional::stream)));
    }

    public Mono<Franchise> renameFranchise(String franchiseId, String newName) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            franchise.rename(newName);
            return repository.save(franchise);
        });
    }

    public Mono<Branch> renameBranch(String franchiseId, String branchId, String newName) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            franchise.getBranch(branchId).rename(newName);
            franchise.touch();
            return repository.save(franchise).map(saved -> saved.getBranch(branchId));
        });
    }

    public Mono<Product> renameProduct(String franchiseId, String branchId, String productId, String newName) {
        return loadFranchise(franchiseId).flatMap(franchise -> {
            franchise.getBranch(branchId).getProduct(productId).rename(newName);
            franchise.touch();
            return repository.save(franchise)
                    .map(saved -> saved.getBranch(branchId).getProduct(productId));
        });
    }

    private Mono<Franchise> loadFranchise(String franchiseId) {
        return repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new FranchiseNotFoundException(franchiseId)));
    }
}
