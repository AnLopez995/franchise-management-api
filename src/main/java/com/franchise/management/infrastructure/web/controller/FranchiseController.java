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

import java.util.List;

/**
 * REST entry point for the franchises API. Holds no business logic: it validates input,
 * delegates to {@link FranchiseService} and maps the result to response DTOs.
 */
@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final FranchiseService service;

    public FranchiseController(FranchiseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FranchiseResponse> createFranchise(
            @Valid @RequestBody CreateFranchiseRequest request) {
        var franchise = service.createFranchise(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(FranchiseWebMapper.toResponse(franchise));
    }

    @PostMapping("/{franchiseId}/branches")
    public ResponseEntity<BranchResponse> addBranch(
            @PathVariable String franchiseId,
            @Valid @RequestBody CreateBranchRequest request) {
        var branch = service.addBranch(franchiseId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(FranchiseWebMapper.toResponse(branch));
    }

    @PostMapping("/{franchiseId}/branches/{branchId}/products")
    public ResponseEntity<ProductResponse> addProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody CreateProductRequest request) {
        var product = service.addProduct(franchiseId, branchId, request.name(), request.stock());
        return ResponseEntity.status(HttpStatus.CREATED).body(FranchiseWebMapper.toResponse(product));
    }

    @DeleteMapping("/{franchiseId}/branches/{branchId}/products/{productId}")
    public ResponseEntity<Void> removeProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId) {
        service.removeProduct(franchiseId, branchId, productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        var product = service.updateStock(franchiseId, branchId, productId, request.stock());
        return ResponseEntity.ok(FranchiseWebMapper.toResponse(product));
    }

    @GetMapping("/{franchiseId}/branches/top-stock-products")
    public ResponseEntity<List<TopStockProductResponse>> topStockProducts(
            @PathVariable String franchiseId) {
        var products = service.getTopStockProducts(franchiseId);
        return ResponseEntity.ok(FranchiseWebMapper.toTopStockResponses(products));
    }

    @PatchMapping("/{franchiseId}/name")
    public ResponseEntity<FranchiseResponse> renameFranchise(
            @PathVariable String franchiseId,
            @Valid @RequestBody UpdateNameRequest request) {
        var franchise = service.renameFranchise(franchiseId, request.name());
        return ResponseEntity.ok(FranchiseWebMapper.toResponse(franchise));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/name")
    public ResponseEntity<BranchResponse> renameBranch(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @Valid @RequestBody UpdateNameRequest request) {
        var branch = service.renameBranch(franchiseId, branchId, request.name());
        return ResponseEntity.ok(FranchiseWebMapper.toResponse(branch));
    }

    @PatchMapping("/{franchiseId}/branches/{branchId}/products/{productId}/name")
    public ResponseEntity<ProductResponse> renameProduct(
            @PathVariable String franchiseId,
            @PathVariable String branchId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateNameRequest request) {
        var product = service.renameProduct(franchiseId, branchId, productId, request.name());
        return ResponseEntity.ok(FranchiseWebMapper.toResponse(product));
    }
}
