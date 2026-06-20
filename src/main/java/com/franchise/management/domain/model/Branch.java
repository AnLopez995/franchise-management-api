package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.ProductNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Branch of a {@link Franchise}. Owns its embedded products and is identified by a UUID.
 */
public class Branch {

    private final String id;
    private String name;
    private final List<Product> products;

    private Branch(String id, String name, List<Product> products) {
        this.id = id;
        this.name = name;
        this.products = products;
    }

    /** Factory for a brand-new branch; generates the UUID and starts with no products. */
    public static Branch create(String name) {
        return new Branch(UUID.randomUUID().toString(), name, new ArrayList<>());
    }

    /** Rehydrates an existing branch (e.g. from persistence) without regenerating its id. */
    public static Branch rehydrate(String id, String name, List<Product> products) {
        return new Branch(id, name, new ArrayList<>(products));
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public Product getProduct(String productId) {
        return products.stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public void removeProduct(String productId) {
        Product product = getProduct(productId);
        products.remove(product);
    }

    public Optional<Product> topStockProduct() {
        return products.stream().max(Comparator.comparingInt(Product::getStock));
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Read-only view of the products held by this branch. */
    public List<Product> getProducts() {
        return List.copyOf(products);
    }
}
