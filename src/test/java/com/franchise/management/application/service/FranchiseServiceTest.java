package com.franchise.management.application.service;

import com.franchise.management.domain.exception.BranchNotFoundException;
import com.franchise.management.domain.exception.BusinessValidationException;
import com.franchise.management.domain.exception.FranchiseNotFoundException;
import com.franchise.management.domain.exception.ProductNotFoundException;
import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import com.franchise.management.domain.port.FranchiseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseServiceTest {

    @Mock
    private FranchiseRepositoryPort repository;

    private FranchiseService service;

    @BeforeEach
    void setUp() {
        service = new FranchiseService(repository);
        // Default: save echoes back the aggregate it was given.
        lenientReturnSavedArgument();
    }

    private void lenientReturnSavedArgument() {
        org.mockito.Mockito.lenient()
                .when(repository.save(any(Franchise.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private Franchise existingFranchise() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        when(repository.findById("f1")).thenReturn(Mono.just(franchise));
        return franchise;
    }

    @Test
    void createFranchisePersistsNewAggregate() {
        Franchise result = service.createFranchise("Franquicia Norte").block();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Franquicia Norte");
        assertThat(result.getBranches()).isEmpty();
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void addBranchAddsAndReturnsBranch() {
        existingFranchise();

        Branch branch = service.addBranch("f1", "Sucursal Centro").block();

        assertThat(branch).isNotNull();
        assertThat(branch.getId()).isNotBlank();
        assertThat(branch.getName()).isEqualTo("Sucursal Centro");
        assertThat(branch.getProducts()).isEmpty();
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void addBranchOnMissingFranchiseThrows() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        assertThatThrownBy(() -> service.addBranch("missing", "Sucursal Centro").block())
                .isInstanceOf(FranchiseNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void addProductAddsAndReturnsProduct() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        franchise.addBranch(branch);

        Product product = service.addProduct("f1", branch.getId(), "Producto A", 50).block();

        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Producto A");
        assertThat(product.getStock()).isEqualTo(50);
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void addProductOnMissingBranchThrows() {
        existingFranchise();

        assertThatThrownBy(() -> service.addProduct("f1", "missing", "Producto A", 50).block())
                .isInstanceOf(BranchNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void addProductRejectsNegativeStock() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        franchise.addBranch(branch);

        assertThatThrownBy(() -> service.addProduct("f1", branch.getId(), "Producto A", -1).block())
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void removeProductDeletesIt() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        service.removeProduct("f1", branch.getId(), product.getId()).block();

        assertThat(franchise.getBranch(branch.getId()).getProducts()).isEmpty();
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void removeProductOnMissingProductThrows() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        franchise.addBranch(branch);

        assertThatThrownBy(() -> service.removeProduct("f1", branch.getId(), "missing").block())
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateStockChangesValue() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        Product updated = service.updateStock("f1", branch.getId(), product.getId(), 120).block();

        assertThat(updated).isNotNull();
        assertThat(updated.getStock()).isEqualTo(120);
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void updateStockRejectsNegativeValue() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        assertThatThrownBy(() -> service.updateStock("f1", branch.getId(), product.getId(), -3).block())
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getTopStockProductsReturnsHighestPerBranchAndSkipsEmptyBranches() {
        Franchise franchise = existingFranchise();

        Branch center = Branch.create("Sucursal Centro");
        center.addProduct(Product.create("Low", 10));
        Product centerTop = Product.create("High", 120);
        center.addProduct(centerTop);
        franchise.addBranch(center);

        Branch north = Branch.create("Sucursal Norte");
        Product northTop = Product.create("Only", 7);
        north.addProduct(northTop);
        franchise.addBranch(north);

        franchise.addBranch(Branch.create("Sucursal Vacia")); // no products -> excluded

        var result = service.getTopStockProducts("f1").collectList().block();

        assertThat(result).containsExactlyInAnyOrder(
                TopStockProduct.of(center, centerTop),
                TopStockProduct.of(north, northTop)
        );
    }

    @Test
    void getTopStockProductsOnMissingFranchiseThrows() {
        when(repository.findById("missing")).thenReturn(Mono.empty());

        assertThatThrownBy(() -> service.getTopStockProducts("missing").collectList().block())
                .isInstanceOf(FranchiseNotFoundException.class);
    }

    @Test
    void renameFranchiseUpdatesName() {
        existingFranchise();

        Franchise result = service.renameFranchise("f1", "Franquicia Sur").block();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Franquicia Sur");
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void renameBranchUpdatesName() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        franchise.addBranch(branch);

        Branch result = service.renameBranch("f1", branch.getId(), "Sucursal Sur").block();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sucursal Sur");
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void renameProductUpdatesName() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        Product result = service.renameProduct("f1", branch.getId(), product.getId(), "Producto B").block();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Producto B");
        verify(repository).save(any(Franchise.class));
    }

    @Test
    void nestedMutationTouchesUpdatedAt() {
        Franchise franchise = existingFranchise();
        Branch branch = Branch.create("Sucursal Centro");
        Product product = Product.create("Producto A", 50);
        branch.addProduct(product);
        franchise.addBranch(branch);

        service.updateStock("f1", branch.getId(), product.getId(), 120).block();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUpdatedAt())
                .isAfterOrEqualTo(captor.getValue().getCreatedAt());
    }
}
