package com.franchise.management.infrastructure.persistence.adapter;

import com.franchise.management.domain.model.Franchise;
import com.franchise.management.domain.port.FranchiseRepositoryPort;
import com.franchise.management.infrastructure.persistence.mapper.FranchiseDocumentMapper;
import com.franchise.management.infrastructure.persistence.repository.FranchiseMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapts the MongoDB repository to the domain {@link FranchiseRepositoryPort}, mapping
 * between the aggregate and its document representation on the way in and out.
 */
@Repository
public class FranchiseRepositoryAdapter implements FranchiseRepositoryPort {

    private final FranchiseMongoRepository mongoRepository;

    public FranchiseRepositoryAdapter(FranchiseMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Franchise save(Franchise franchise) {
        var saved = mongoRepository.save(FranchiseDocumentMapper.toDocument(franchise));
        return FranchiseDocumentMapper.toDomain(saved);
    }

    @Override
    public Optional<Franchise> findById(String id) {
        return mongoRepository.findById(id).map(FranchiseDocumentMapper::toDomain);
    }
}
