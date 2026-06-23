package com.franchise.management.infrastructure.web.controller;

import com.franchise.management.application.service.FranchiseService;
import com.franchise.management.domain.exception.BranchNotFoundException;
import com.franchise.management.domain.exception.FranchiseNotFoundException;
import com.franchise.management.domain.exception.ProductNotFoundException;
import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranchiseService service;

    @Test
    void createFranchiseReturns201WithBody() {
        when(service.createFranchise("Franquicia Norte"))
                .thenReturn(Mono.just(Franchise.rehydrate("f1", "Franquicia Norte", List.of(),
                        LocalDateTime.now(), LocalDateTime.now(), 0L)));

        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Franquicia Norte"}""")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.name").isEqualTo("Franquicia Norte")
                .jsonPath("$.branches").isEmpty();
    }

    @Test
    void createFranchiseWithBlankNameReturns400() {
        webTestClient.post().uri("/api/v1/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "  "}""")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("BAD_REQUEST");
    }

    @Test
    void addBranchReturns201() {
        when(service.addBranch(eq("f1"), eq("Sucursal Centro")))
                .thenReturn(Mono.just(Branch.create("Sucursal Centro")));

        webTestClient.post().uri("/api/v1/franchises/f1/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Sucursal Centro"}""")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Sucursal Centro")
                .jsonPath("$.products").isEmpty();
    }

    @Test
    void addBranchOnMissingFranchiseReturns404() {
        when(service.addBranch(eq("missing"), any()))
                .thenReturn(Mono.error(new FranchiseNotFoundException("missing")));

        webTestClient.post().uri("/api/v1/franchises/missing/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Sucursal Centro"}""")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("NOT_FOUND")
                .jsonPath("$.message").isEqualTo("Franchise not found with id: missing")
                .jsonPath("$.path").isEqualTo("/api/v1/franchises/missing/branches")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void addProductReturns201() {
        when(service.addProduct(eq("f1"), eq("b1"), eq("Producto A"), eq(50)))
                .thenReturn(Mono.just(Product.create("Producto A", 50)));

        webTestClient.post().uri("/api/v1/franchises/f1/branches/b1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Producto A", "stock": 50}""")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Producto A")
                .jsonPath("$.stock").isEqualTo(50);
    }

    @Test
    void addProductWithNegativeStockReturns400() {
        webTestClient.post().uri("/api/v1/franchises/f1/branches/b1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Producto A", "stock": -1}""")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void addProductOnMissingBranchReturns404() {
        when(service.addProduct(eq("f1"), eq("missing"), anyString(), anyInt()))
                .thenReturn(Mono.error(new BranchNotFoundException("missing")));

        webTestClient.post().uri("/api/v1/franchises/f1/branches/missing/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Producto A", "stock": 50}""")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("NOT_FOUND");
    }

    @Test
    void deleteProductReturns204() {
        when(service.removeProduct("f1", "b1", "p1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/franchises/f1/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNoContent();

        verify(service).removeProduct("f1", "b1", "p1");
    }

    @Test
    void deleteMissingProductReturns404() {
        when(service.removeProduct("f1", "b1", "p1"))
                .thenReturn(Mono.error(new ProductNotFoundException("p1")));

        webTestClient.delete().uri("/api/v1/franchises/f1/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateStockReturns200() {
        Product product = Product.create("Producto A", 50);
        product.changeStock(120);
        when(service.updateStock(eq("f1"), eq("b1"), eq("p1"), eq(120))).thenReturn(Mono.just(product));

        webTestClient.patch().uri("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"stock": 120}""")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(120);
    }

    @Test
    void updateStockWithNegativeValueReturns400() {
        webTestClient.patch().uri("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"stock": -5}""")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void topStockProductsReturns200WithList() {
        when(service.getTopStockProducts("f1")).thenReturn(Flux.just(
                new TopStockProduct("b1", "Sucursal Centro", "p1", "Producto A", 120)));

        webTestClient.get().uri("/api/v1/franchises/f1/branches/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchId").isEqualTo("b1")
                .jsonPath("$[0].branchName").isEqualTo("Sucursal Centro")
                .jsonPath("$[0].productId").isEqualTo("p1")
                .jsonPath("$[0].productName").isEqualTo("Producto A")
                .jsonPath("$[0].stock").isEqualTo(120);
    }

    @Test
    void renameFranchiseReturns200() {
        when(service.renameFranchise("f1", "Franquicia Sur"))
                .thenReturn(Mono.just(Franchise.rehydrate("f1", "Franquicia Sur", List.of(),
                        LocalDateTime.now(), LocalDateTime.now(), 0L)));

        webTestClient.patch().uri("/api/v1/franchises/f1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Franquicia Sur"}""")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Franquicia Sur");
    }

    @Test
    void renameBranchReturns200() {
        Branch branch = Branch.create("Sucursal Sur");
        when(service.renameBranch(eq("f1"), eq("b1"), eq("Sucursal Sur"))).thenReturn(Mono.just(branch));

        webTestClient.patch().uri("/api/v1/franchises/f1/branches/b1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Sucursal Sur"}""")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Sucursal Sur");
    }

    @Test
    void renameProductReturns200() {
        Product product = Product.create("Producto B", 50);
        when(service.renameProduct(eq("f1"), eq("b1"), eq("p1"), eq("Producto B")))
                .thenReturn(Mono.just(product));

        webTestClient.patch().uri("/api/v1/franchises/f1/branches/b1/products/p1/name")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name": "Producto B"}""")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Producto B");
    }
}
