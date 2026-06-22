package com.franchise.management.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Root MongoDB document for the franchise aggregate. Branches and products are stored
 * embedded, so the whole aggregate is read and written as a single document.
 *
 * <p>{@code version} drives optimistic locking: Spring Data conditions each update on the
 * stored version, so a concurrent write over a stale aggregate fails instead of silently
 * overwriting (lost update).
 *
 * <p>The multikey indexes on the embedded ids ({@code branches._id}, {@code branches.products._id}
 * — the nested {@code id} fields are stored by Mongo as {@code _id}) keep locating a branch/product
 * within the arrays from degrading to a full document scan as the aggregate grows. They are declared
 * here (reliable on the root) rather than via {@code @Indexed} on the nested records, and only
 * created when {@code spring.data.mongodb.auto-index-creation} is enabled for the environment.
 */
@Document(collection = "franchises")
@CompoundIndexes({
        @CompoundIndex(name = "branch_id_idx", def = "{'branches._id': 1}"),
        @CompoundIndex(name = "product_id_idx", def = "{'branches.products._id': 1}")
})
public record FranchiseDocument(
        @Id String id,
        String name,
        List<BranchDocument> branches,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @Version Long version
) {
}
