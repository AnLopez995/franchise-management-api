package com.franchise.management.infrastructure.persistence.repository;

import com.franchise.management.infrastructure.persistence.document.FranchiseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for {@link FranchiseDocument}. CRUD is enough since the
 * whole aggregate lives in a single document.
 */
public interface FranchiseMongoRepository extends MongoRepository<FranchiseDocument, String> {
}
