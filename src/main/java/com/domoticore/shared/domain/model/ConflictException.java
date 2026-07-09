package com.domoticore.shared.domain.model;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
