package com.franchise.management.infrastructure.persistence.document;

import java.util.List;

/**
 * Branch as embedded inside a {@link FranchiseDocument}, holding its products inline.
 */
public record BranchDocument(String id, String name, List<ProductDocument> products) {
}
