package com.franchise.management.domain.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(String productId) {
        super("Product not found with id: " + productId);
    }
}
