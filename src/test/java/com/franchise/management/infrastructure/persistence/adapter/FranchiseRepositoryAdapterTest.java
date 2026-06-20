package com.franchise.management.infrastructure.persistence.adapter;

import com.franchise.management.domain.model.Franchise;
import com.franchise.management.infrastructure.persistence.document.FranchiseDocument;
import com.franchise.management.infrastructure.persistence.repository.FranchiseMongoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseRepositoryAdapterTest {

    @Mock
    private FranchiseMongoRepository mongoRepository;

    private FranchiseRepositoryAdapter adapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adapter = new FranchiseRepositoryAdapter(mongoRepository);
    }

    @Test
    void saveMapsToDocumentAndReturnsDomainWithGeneratedId() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        when(mongoRepository.save(any(FranchiseDocument.class)))
                .thenAnswer(invocation -> {
                    FranchiseDocument input = invocation.getArgument(0);
                    return new FranchiseDocument("generated-id", input.name(),
                            input.branches(), input.createdAt(), input.updatedAt());
                });

        Franchise result = adapter.save(franchise);

        ArgumentCaptor<FranchiseDocument> captor = ArgumentCaptor.forClass(FranchiseDocument.class);
        verify(mongoRepository).save(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Franquicia Norte");
        assertThat(result.getId()).isEqualTo("generated-id");
        assertThat(result.getName()).isEqualTo("Franquicia Norte");
    }

    @Test
    void findByIdReturnsMappedDomainWhenPresent() {
        LocalDateTime now = LocalDateTime.now();
        FranchiseDocument document = new FranchiseDocument("f1", "Franquicia Norte",
                List.of(), now, now);
        when(mongoRepository.findById("f1")).thenReturn(Optional.of(document));

        Optional<Franchise> result = adapter.findById("f1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("f1");
        assertThat(result.get().getName()).isEqualTo("Franquicia Norte");
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        when(mongoRepository.findById("missing")).thenReturn(Optional.empty());

        assertThat(adapter.findById("missing")).isEmpty();
    }
}
