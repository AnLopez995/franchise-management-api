package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BranchTest {

    @Test
    void createGeneratesUuidAndEmptyProducts() {
        Branch branch = Branch.create("Sucursal Centro");

        assertThat(branch.getId()).isNotBlank();
        assertThat(branch.getName()).isEqualTo("Sucursal Centro");
        assertThat(branch.getProducts()).isEmpty();
    }

    @Test
    void addProductStoresIt() {
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);

        branch.addProduct(product);

        assertThat(branch.getProducts()).containsExactly(product);
    }

    @Test
    void getProductReturnsMatchingProduct() {
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);

        assertThat(branch.getProduct(product.getId())).isSameAs(product);
    }

    @Test
    void getProductThrowsWhenMissing() {
        Branch branch = Branch.create("Sucursal Centro");

        assertThatThrownBy(() -> branch.getProduct("missing"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void removeProductDeletesIt() {
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);

        branch.removeProduct(product.getId());

        assertThat(branch.getProducts()).isEmpty();
    }

    @Test
    void removeProductThrowsWhenMissing() {
        Branch branch = Branch.create("Sucursal Centro");

        assertThatThrownBy(() -> branch.removeProduct("missing"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void topStockProductReturnsHighestStock() {
        Branch branch = Branch.create("Sucursal Centro");
        branch.addProduct(Product.create("Low", 10));
        Product top = Product.create("High", 99);
        branch.addProduct(top);

        assertThat(branch.topStockProduct()).contains(top);
    }

    @Test
    void topStockProductIsEmptyWhenNoProducts() {
        Branch branch = Branch.create("Sucursal Centro");

        assertThat(branch.topStockProduct()).isEqualTo(Optional.empty());
    }

    @Test
    void renameUpdatesName() {
        Branch branch = Branch.create("Sucursal Centro");

        branch.rename("Sucursal Norte");

        assertThat(branch.getName()).isEqualTo("Sucursal Norte");
    }
}
