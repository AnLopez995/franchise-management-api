package com.franchise.management.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.franchise.management.application.service.FranchiseService;
import com.franchise.management.domain.exception.BranchNotFoundException;
import com.franchise.management.domain.exception.FranchiseNotFoundException;
import com.franchise.management.domain.model.Branch;
import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.model.Product;
import com.franchise.management.domain.model.TopStockProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FranchiseController.class)
class FranchiseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FranchiseService service;

    @Test
    void createFranchiseReturns201WithBody() throws Exception {
        when(service.createFranchise("Franquicia Norte"))
                .thenReturn(Franchise.rehydrate("f1", "Franquicia Norte", List.of(),
                        LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Franquicia Norte"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("f1"))
                .andExpect(jsonPath("$.name").value("Franquicia Norte"))
                .andExpect(jsonPath("$.branches").isEmpty());
    }

    @Test
    void createFranchiseWithBlankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/franchises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "  "}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void addBranchReturns201() throws Exception {
        when(service.addBranch(eq("f1"), eq("Sucursal Centro")))
                .thenReturn(Branch.create("Sucursal Centro"));

        mockMvc.perform(post("/api/v1/franchises/f1/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Sucursal Centro"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sucursal Centro"))
                .andExpect(jsonPath("$.products").isEmpty());
    }

    @Test
    void addBranchOnMissingFranchiseReturns404() throws Exception {
        when(service.addBranch(eq("missing"), any()))
                .thenThrow(new FranchiseNotFoundException("missing"));

        mockMvc.perform(post("/api/v1/franchises/missing/branches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Sucursal Centro"}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Franchise not found with id: missing"))
                .andExpect(jsonPath("$.path").value("/api/v1/franchises/missing/branches"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addProductReturns201() throws Exception {
        when(service.addProduct(eq("f1"), eq("b1"), eq("Producto A"), eq(50)))
                .thenReturn(Product.create("Producto A", 50));

        mockMvc.perform(post("/api/v1/franchises/f1/branches/b1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Producto A", "stock": 50}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Producto A"))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    void addProductWithNegativeStockReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/franchises/f1/branches/b1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Producto A", "stock": -1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addProductOnMissingBranchReturns404() throws Exception {
        when(service.addProduct(eq("f1"), eq("missing"), anyString(), anyInt()))
                .thenThrow(new BranchNotFoundException("missing"));

        mockMvc.perform(post("/api/v1/franchises/f1/branches/missing/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Producto A", "stock": 50}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void deleteProductReturns204() throws Exception {
        doNothing().when(service).removeProduct("f1", "b1", "p1");

        mockMvc.perform(delete("/api/v1/franchises/f1/branches/b1/products/p1"))
                .andExpect(status().isNoContent());

        verify(service).removeProduct("f1", "b1", "p1");
    }

    @Test
    void deleteMissingProductReturns404() throws Exception {
        doThrow(new com.franchise.management.domain.exception.ProductNotFoundException("p1"))
                .when(service).removeProduct("f1", "b1", "p1");

        mockMvc.perform(delete("/api/v1/franchises/f1/branches/b1/products/p1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStockReturns200() throws Exception {
        Product product = Product.create("Producto A", 50);
        product.changeStock(120);
        when(service.updateStock(eq("f1"), eq("b1"), eq("p1"), eq(120))).thenReturn(product);

        mockMvc.perform(patch("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"stock": 120}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(120));
    }

    @Test
    void updateStockWithNegativeValueReturns400() throws Exception {
        mockMvc.perform(patch("/api/v1/franchises/f1/branches/b1/products/p1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"stock": -5}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void topStockProductsReturns200WithList() throws Exception {
        when(service.getTopStockProducts("f1")).thenReturn(List.of(
                new TopStockProduct("b1", "Sucursal Centro", "p1", "Producto A", 120)));

        mockMvc.perform(get("/api/v1/franchises/f1/branches/top-stock-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchId").value("b1"))
                .andExpect(jsonPath("$[0].branchName").value("Sucursal Centro"))
                .andExpect(jsonPath("$[0].productId").value("p1"))
                .andExpect(jsonPath("$[0].productName").value("Producto A"))
                .andExpect(jsonPath("$[0].stock").value(120));
    }

    @Test
    void renameFranchiseReturns200() throws Exception {
        when(service.renameFranchise("f1", "Franquicia Sur"))
                .thenReturn(Franchise.rehydrate("f1", "Franquicia Sur", List.of(),
                        LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(patch("/api/v1/franchises/f1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Franquicia Sur"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Franquicia Sur"));
    }

    @Test
    void renameBranchReturns200() throws Exception {
        Branch branch = Branch.create("Sucursal Sur");
        when(service.renameBranch(eq("f1"), eq("b1"), eq("Sucursal Sur"))).thenReturn(branch);

        mockMvc.perform(patch("/api/v1/franchises/f1/branches/b1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Sucursal Sur"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sucursal Sur"));
    }

    @Test
    void renameProductReturns200() throws Exception {
        Product product = Product.create("Producto B", 50);
        when(service.renameProduct(eq("f1"), eq("b1"), eq("p1"), eq("Producto B"))).thenReturn(product);

        mockMvc.perform(patch("/api/v1/franchises/f1/branches/b1/products/p1/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Producto B"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Producto B"));
    }
}
