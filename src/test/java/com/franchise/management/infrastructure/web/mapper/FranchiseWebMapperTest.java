package com.franchise.management.infrastructure.web.mapper;

import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import com.franchise.management.infrastructure.web.dto.response.FranchiseResponse;
import com.franchise.management.infrastructure.web.dto.response.TopStockProductResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseWebMapperTest {

    @Test
    void toFranchiseResponseIncludesNestedBranchesAndProducts() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        FranchiseResponse response = FranchiseWebMapper.toResponse(franchise);

        assertThat(response.name()).isEqualTo("Franquicia Norte");
        assertThat(response.branches()).hasSize(1);
        assertThat(response.branches().get(0).name()).isEqualTo("Sucursal Centro");
        assertThat(response.branches().get(0).products()).hasSize(1);
        assertThat(response.branches().get(0).products().get(0).name()).isEqualTo("Producto A");
        assertThat(response.branches().get(0).products().get(0).stock()).isEqualTo(50);
    }

    @Test
    void newFranchiseResponseHasEmptyBranches() {
        FranchiseResponse response = FranchiseWebMapper.toResponse(Franchise.create("Franquicia Norte"));

        assertThat(response.branches()).isEmpty();
    }

    @Test
    void toTopStockResponseMapsAllFields() {
        TopStockProduct projection = new TopStockProduct("b1", "Sucursal Centro", "p1", "Producto A", 120);

        TopStockProductResponse response = FranchiseWebMapper.toResponse(projection);

        assertThat(response.branchId()).isEqualTo("b1");
        assertThat(response.branchName()).isEqualTo("Sucursal Centro");
        assertThat(response.productId()).isEqualTo("p1");
        assertThat(response.productName()).isEqualTo("Producto A");
        assertThat(response.stock()).isEqualTo(120);
    }
}
