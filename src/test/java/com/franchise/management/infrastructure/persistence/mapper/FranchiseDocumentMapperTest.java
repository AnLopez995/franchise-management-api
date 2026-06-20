package com.franchise.management.infrastructure.persistence.mapper;

import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.infrastructure.persistence.document.FranchiseDocument;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseDocumentMapperTest {

    @Test
    void toDocumentMapsNestedStructure() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        FranchiseDocument document = FranchiseDocumentMapper.toDocument(franchise);

        assertThat(document.name()).isEqualTo("Franquicia Norte");
        assertThat(document.createdAt()).isEqualTo(franchise.getCreatedAt());
        assertThat(document.updatedAt()).isEqualTo(franchise.getUpdatedAt());
        assertThat(document.branches()).hasSize(1);
        assertThat(document.branches().get(0).id()).isEqualTo(branch.getId());
        assertThat(document.branches().get(0).name()).isEqualTo("Sucursal Centro");
        assertThat(document.branches().get(0).products()).hasSize(1);
        assertThat(document.branches().get(0).products().get(0).id()).isEqualTo(product.getId());
        assertThat(document.branches().get(0).products().get(0).name()).isEqualTo("Producto A");
        assertThat(document.branches().get(0).products().get(0).stock()).isEqualTo(50);
    }

    @Test
    void roundTripPreservesIdsTimestampsAndValues() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        FranchiseDocument document = FranchiseDocumentMapper.toDocument(franchise);
        // Simulate the id Mongo would assign on persistence.
        FranchiseDocument persisted = new FranchiseDocument(
                "generated-id", document.name(), document.branches(),
                document.createdAt(), document.updatedAt());

        Franchise restored = FranchiseDocumentMapper.toDomain(persisted);

        assertThat(restored.getId()).isEqualTo("generated-id");
        assertThat(restored.getName()).isEqualTo("Franquicia Norte");
        assertThat(restored.getCreatedAt()).isEqualTo(franchise.getCreatedAt());
        assertThat(restored.getUpdatedAt()).isEqualTo(franchise.getUpdatedAt());

        Branch restoredBranch = restored.getBranch(branch.getId());
        assertThat(restoredBranch.getName()).isEqualTo("Sucursal Centro");

        Product restoredProduct = restoredBranch.getProduct(product.getId());
        assertThat(restoredProduct.getName()).isEqualTo("Producto A");
        assertThat(restoredProduct.getStock()).isEqualTo(50);
    }
}
