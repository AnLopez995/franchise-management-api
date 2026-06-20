package com.franchise.management.domain.model;

import com.franchise.management.domain.exception.BranchNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FranchiseTest {

    @Test
    void createStartsWithEmptyBranchesAndTimestamps() {
        Franchise franchise = Franchise.create("Franquicia Norte");

        assertThat(franchise.getName()).isEqualTo("Franquicia Norte");
        assertThat(franchise.getBranches()).isEmpty();
        assertThat(franchise.getCreatedAt()).isNotNull();
        assertThat(franchise.getUpdatedAt()).isEqualTo(franchise.getCreatedAt());
    }

    @Test
    void addBranchStoresItAndTouchesUpdatedAt() throws InterruptedException {
        Franchise franchise = Franchise.create("Franquicia Norte");
        var createdAt = franchise.getUpdatedAt();
        Branch branch = Branch.create("Sucursal Centro");
        Thread.sleep(1);

        franchise.addBranch(branch);

        assertThat(franchise.getBranches()).containsExactly(branch);
        assertThat(franchise.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    void getBranchReturnsMatchingBranch() {
        Franchise franchise = Franchise.create("Franquicia Norte");
        Branch branch = Branch.create("Sucursal Centro");
        franchise.addBranch(branch);

        assertThat(franchise.getBranch(branch.getId())).isSameAs(branch);
    }

    @Test
    void getBranchThrowsWhenMissing() {
        Franchise franchise = Franchise.create("Franquicia Norte");

        assertThatThrownBy(() -> franchise.getBranch("missing"))
                .isInstanceOf(BranchNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void renameUpdatesNameAndTouchesUpdatedAt() throws InterruptedException {
        Franchise franchise = Franchise.create("Franquicia Norte");
        var before = franchise.getUpdatedAt();
        Thread.sleep(1);

        franchise.rename("Franquicia Sur");

        assertThat(franchise.getName()).isEqualTo("Franquicia Sur");
        assertThat(franchise.getUpdatedAt()).isAfter(before);
    }

    @Test
    void touchAdvancesUpdatedAtOnly() throws InterruptedException {
        Franchise franchise = Franchise.create("Franquicia Norte");
        var createdAt = franchise.getCreatedAt();
        Thread.sleep(1);

        franchise.touch();

        assertThat(franchise.getCreatedAt()).isEqualTo(createdAt);
        assertThat(franchise.getUpdatedAt()).isAfter(createdAt);
    }
}
