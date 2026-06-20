package com.franchise.management.infrastructure.persistence.mapper;

import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.infrastructure.persistence.document.BranchDocument;
import com.franchise.management.infrastructure.persistence.document.FranchiseDocument;
import com.franchise.management.infrastructure.persistence.document.ProductDocument;

import java.util.List;

/**
 * Translates between the {@link Franchise} domain aggregate and its MongoDB document form.
 * Stateless; uses the domain {@code rehydrate} factories so ids and timestamps survive a round trip.
 */
public final class FranchiseDocumentMapper {

    private FranchiseDocumentMapper() {
    }

    public static FranchiseDocument toDocument(Franchise franchise) {
        List<BranchDocument> branches = franchise.getBranches().stream()
                .map(FranchiseDocumentMapper::toBranchDocument)
                .toList();
        return new FranchiseDocument(
                franchise.getId(),
                franchise.getName(),
                branches,
                franchise.getCreatedAt(),
                franchise.getUpdatedAt());
    }

    public static Franchise toDomain(FranchiseDocument document) {
        List<Branch> branches = document.branches().stream()
                .map(FranchiseDocumentMapper::toBranchDomain)
                .toList();
        return Franchise.rehydrate(
                document.id(),
                document.name(),
                branches,
                document.createdAt(),
                document.updatedAt());
    }

    private static BranchDocument toBranchDocument(Branch branch) {
        List<ProductDocument> products = branch.getProducts().stream()
                .map(product -> new ProductDocument(product.getId(), product.getName(), product.getStock()))
                .toList();
        return new BranchDocument(branch.getId(), branch.getName(), products);
    }

    private static Branch toBranchDomain(BranchDocument document) {
        List<Product> products = document.products().stream()
                .map(product -> Product.rehydrate(product.id(), product.name(), product.stock()))
                .toList();
        return Branch.rehydrate(document.id(), document.name(), products);
    }
}
