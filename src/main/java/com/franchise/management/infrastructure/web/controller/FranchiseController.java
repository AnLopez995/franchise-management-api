package com.franchise.management.infrastructure.web.controller;

import com.franchise.management.application.service.FranchiseService;
import com.franchise.management.infrastructure.web.dto.request.CreateBranchRequest;
import com.franchise.management.infrastructure.web.dto.request.CreateFranchiseRequest;
import com.franchise.management.infrastructure.web.dto.request.CreateProductRequest;
import com.franchise.management.infrastructure.web.dto.request.UpdateNameRequest;
import com.franchise.management.infrastructure.web.dto.request.UpdateStockRequest;
import com.franchise.management.infrastructure.web.dto.response.BranchResponse;
import com.franchise.management.infrastructure.web.dto.response.FranchiseResponse;
import com.franchise.management.infrastructure.web.dto.response.ProductResponse;
import com.franchise.management.infrastructure.web.dto.response.TopStockProductResponse;
import com.franchise.management.infrastructure.web.mapper.FranchiseWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST entry point for the franchises API. Holds no business logic: it validates input,
 * delegates to {@link FranchiseService} and maps the reactive result to response DTOs.
 */
@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final FranchiseService service;

    public FranchiseController(FranchiseService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<FranchiseResponse>> createFranchise(
            @Valid @RequestBody CreateFranchiseRequest request) {
        return service.createFranchise(request.name())
                .map(FranchiseWebMapper::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/{franchiseId}/branches")
    public Mono<ResponseEntity<BranchResponse>> addBranch(
            @PathVariable String franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        return service.addBranch(franchiseId, request.name())
                .map(FranchiseWebMapper::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/{franchiseId}/branches/{branchId}/products")
    public Mono<ResponseEntity<ProductResponse>> addProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody CreateProductRequest request) {
        return service.addProduct(franchiseId, branchId, request.name(), request.stock())
                .map(FranchiseWebMapper::toResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    public Mono<ResponseEntity<Void>> removeProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId) {
        return service.removeProduct(franchiseId, branchId, productId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    public Mono<ResponseEntity<ProductResponse>> updateStock(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        return service.updateStock(franchiseId, branchId, productId, request.stock())
                .map(FranchiseWebMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{franchiseId}/branches/top-stock-products")
    public Flux<TopStockProductResponse> topStockProducts(@PathVariable String franchiseId) {
        return service.getTopStockProducts(franchiseId).map(FranchiseWebMapper::toResponse);
    }

    @PatchMapping("/{franchiseId}/name")
    public Mono<ResponseEntity<FranchiseResponse>> renameFranchise(
            @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        return service.renameFranchise(franchiseId, request.name())
                .map(FranchiseWebMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/name")
    public Mono<ResponseEntity<BranchResponse>> renameBranch(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        return service.renameBranch(franchiseId, branchId, request.name())
                .map(FranchiseWebMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/name")
    public Mono<ResponseEntity<ProductResponse>> renameProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        return service.renameProduct(franchiseId, branchId, productId, request.name())
                .map(FranchiseWebMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
