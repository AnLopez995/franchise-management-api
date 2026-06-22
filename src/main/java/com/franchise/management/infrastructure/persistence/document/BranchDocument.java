package com.franchise.management.infrastructure.persistence.document;

import java.util.List;

/**
 * Branch as embedded inside a {@link FranchiseDocument}, holding its products inline.
 * The index over its {@code id} (stored path {@code branches._id}) is declared on
 * {@link FranchiseDocument}.
 */
public record BranchDocument(String id, String name, List<ProductDocument> products) {
}
