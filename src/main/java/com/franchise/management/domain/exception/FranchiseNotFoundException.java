package com.franchise.management.domain.exception;

public class FranchiseNotFoundException extends ResourceNotFoundException {

    public FranchiseNotFoundException(String franchiseId) {
        super("Franchise not found with id: " + franchiseId);
    }
}
