package com.franchise.management.infrastructure.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Root MongoDB document for the franchise aggregate. Branches and products are stored
 * embedded, so the whole aggregate is read and written as a single document.
 */
@Document(collection = "franchises")
public record FranchiseDocument(
        @Id String id,
        String name,
        List<BranchDocument> branches,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
