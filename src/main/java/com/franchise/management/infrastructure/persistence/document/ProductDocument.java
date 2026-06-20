package com.franchise.management.infrastructure.persistence.document;

/**
 * Product as embedded inside a {@link BranchDocument}. Plain value, no own collection.
 */
public record ProductDocument(String id, String name, int stock) {
}
