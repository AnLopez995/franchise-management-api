package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void createGeneratesUuidAndKeepsValues() {
        Product product = Product.create("Producto A", 50);

        assertThat(product.getId()).isNotBlank();
        assertThat(product.getName()).isEqualTo("Producto A");
        assertThat(product.getStock()).isEqualTo(50);
    }

    @Test
    void createWithNegativeStockIsRejected() {
        assertThatThrownBy(() -> Product.create("Producto A", -1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void changeStockUpdatesValue() {
        Product product = Product.create("Producto A", 50);

        product.changeStock(120);

        assertThat(product.getStock()).isEqualTo(120);
    }

    @Test
    void changeStockRejectsNegativeValue() {
        Product product = Product.create("Producto A", 50);

        assertThatThrownBy(() -> product.changeStock(-5))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void renameUpdatesName() {
        Product product = Product.create("Producto A", 50);

        product.rename("Producto B");

        assertThat(product.getName()).isEqualTo("Producto B");
    }
}
