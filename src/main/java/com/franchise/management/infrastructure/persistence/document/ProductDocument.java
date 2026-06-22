package com.franchise.management.infrastructure.persistence.document;

/**
 * Product as embedded inside a {@link BranchDocument}. Plain value, no own collection.
 * The index over its {@code id} (stored path {@code branches.products._id}) is declared on
 * {@link FranchiseDocument}.
 */
public record ProductDocument(String id, String name, int stock) {
}
