package com.franchise.management.infrastructure.web.mapper;

import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import com.franchise.management.infrastructure.web.dto.response.BranchResponse;
import com.franchise.management.infrastructure.web.dto.response.FranchiseResponse;
import com.franchise.management.infrastructure.web.dto.response.ProductResponse;
import com.franchise.management.infrastructure.web.dto.response.TopStockProductResponse;

import java.util.List;

/**
 * Maps domain models to the HTTP response DTOs exposed by the API. Stateless.
 */
public final class FranchiseWebMapper {

    private FranchiseWebMapper() {
    }

    public static FranchiseResponse toResponse(Franchise franchise) {
        List<BranchResponse> branches = franchise.getBranches().stream()
                .map(FranchiseWebMapper::toResponse)
                .toList();
        return new FranchiseResponse(franchise.getId(), franchise.getName(), branches);
    }

    public static BranchResponse toResponse(Branch branch) {
        List<ProductResponse> products = branch.getProducts().stream()
                .map(FranchiseWebMapper::toResponse)
                .toList();
        return new BranchResponse(branch.getId(), branch.getName(), products);
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getStock());
    }

    public static TopStockProductResponse toResponse(TopStockProduct projection) {
        return new TopStockProductResponse(
                projection.branchId(),
                projection.branchName(),
                projection.productId(),
                projection.productName(),
                projection.stock());
    }

    public static List<TopStockProductResponse> toTopStockResponses(List<TopStockProduct> projections) {
        return projections.stream().map(FranchiseWebMapper::toResponse).toList();
    }
}
