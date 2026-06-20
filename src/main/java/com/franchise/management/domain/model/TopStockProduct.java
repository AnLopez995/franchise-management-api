package com.franchise.management.domain.model;

/**
 * Read projection: the highest-stock product of a given branch.
 * Returned by the "top stock products per branch" use case.
 */
public record TopStockProduct(
        String branchId,
        String branchName,
        String productId,
        String productName,
        int stock
) {
    public static TopStockProduct of(Branch branch, Product product) {
        return new TopStockProduct(
                branch.getId(),
                branch.getName(),
                product.getId(),
                product.getName(),
                product.getStock()
        );
    }
}
