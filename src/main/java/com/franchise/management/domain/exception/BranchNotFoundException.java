package com.franchise.management.domain.exception;

public class BranchNotFoundException extends ResourceNotFoundException {

    public BranchNotFoundException(String branchId) {
        super("Branch not found with id: " + branchId);
    }
}
