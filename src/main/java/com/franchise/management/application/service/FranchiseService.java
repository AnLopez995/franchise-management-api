package com.franchise.management.application.service;

import com.franchise.management.domain.exception.FranchiseNotFoundException;
import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import com.franchise.management.domain.port.FranchiseRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service orchestrating the franchise use cases. Loads the {@link Franchise}
 * aggregate through the port, applies the requested change on the domain model, refreshes
 * {@code updatedAt} and persists the result. No business rules live here beyond orchestration.
 */
@Service
public class FranchiseService {

    private final FranchiseRepositoryPort repository;

    public FranchiseService(FranchiseRepositoryPort repository) {
        this.repository = repository;
    }

    public Franchise createFranchise(String name) {
        return repository.save(Franchise.create(name));
    }

    public Branch addBranch(String franchiseId, String branchName) {
        Franchise franchise = loadFranchise(franchiseId);
        Branch branch = Branch.create(branchName);
        franchise.addBranch(branch);
        return saveAndGetBranch(franchise, branch.getId());
    }

    public Product addProduct(String franchiseId, String branchId, String productName, int stock) {
        Franchise franchise = loadFranchise(franchiseId);
        Branch branch = franchise.getBranch(branchId);
        Product product = Product.create(productName, stock);
        branch.addProduct(product);
        franchise.touch();
        return saveAndGetProduct(franchise, branchId, product.getId());
    }

    public void removeProduct(String franchiseId, String branchId, String productId) {
        Franchise franchise = loadFranchise(franchiseId);
        franchise.getBranch(branchId).removeProduct(productId);
        franchise.touch();
        repository.save(franchise);
    }

    public Product updateStock(String franchiseId, String branchId, String productId, int stock) {
        Franchise franchise = loadFranchise(franchiseId);
        franchise.getBranch(branchId).getProduct(productId).changeStock(stock);
        franchise.touch();
        return saveAndGetProduct(franchise, branchId, productId);
    }

    public List<TopStockProduct> getTopStockProducts(String franchiseId) {
        Franchise franchise = loadFranchise(franchiseId);
        return franchise.getBranches().stream()
                .map(branch -> branch.topStockProduct()
                        .map(product -> TopStockProduct.of(branch, product)))
                .flatMap(Optional::stream)
                .toList();
    }

    public Franchise renameFranchise(String franchiseId, String newName) {
        Franchise franchise = loadFranchise(franchiseId);
        franchise.rename(newName);
        return repository.save(franchise);
    }

    public Branch renameBranch(String franchiseId, String branchId, String newName) {
        Franchise franchise = loadFranchise(franchiseId);
        franchise.getBranch(branchId).rename(newName);
        franchise.touch();
        return saveAndGetBranch(franchise, branchId);
    }

    public Product renameProduct(String franchiseId, String branchId, String productId, String newName) {
        Franchise franchise = loadFranchise(franchiseId);
        franchise.getBranch(branchId).getProduct(productId).rename(newName);
        franchise.touch();
        return saveAndGetProduct(franchise, branchId, productId);
    }

    private Franchise loadFranchise(String franchiseId) {
        return repository.findById(franchiseId)
                .orElseThrow(() -> new FranchiseNotFoundException(franchiseId));
    }

    private Branch saveAndGetBranch(Franchise franchise, String branchId) {
        return repository.save(franchise).getBranch(branchId);
    }

    private Product saveAndGetProduct(Franchise franchise, String branchId, String productId) {
        return repository.save(franchise).getBranch(branchId).getProduct(productId);
    }
}
