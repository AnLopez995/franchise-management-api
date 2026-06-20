package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.BusinessValidationException;

import java.util.UUID;

/**
 * Product offered within a {@link Branch}. Identified by a UUID generated on creation.
 */
public class Product {

    private final String id;
    private String name;
    private int stock;

    private Product(String id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }

    /** Factory for a brand-new product; generates the UUID and enforces a non-negative stock. */
    public static Product create(String name, int stock) {
        validateStock(stock);
        return new Product(UUID.randomUUID().toString(), name, stock);
    }

    /** Rehydrates an existing product (e.g. from persistence) without regenerating its id. */
    public static Product rehydrate(String id, String name, int stock) {
        return new Product(id, name, stock);
    }

    public void changeStock(int newStock) {
        validateStock(newStock);
        this.stock = newStock;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    private static void validateStock(int stock) {
        if (stock < 0) {
            throw new BusinessValidationException("Stock cannot be negative");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }
}
