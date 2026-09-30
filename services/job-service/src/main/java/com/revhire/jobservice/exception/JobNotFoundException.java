package com.revhire.jobservice.exception;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(Long id) {
        super("Job not found with id: " + id);
    }
}