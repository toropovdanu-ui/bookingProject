package com.skillbox.web.exception;

public class InsufficientTotalTicketsException extends RuntimeException {
    public InsufficientTotalTicketsException(String message) {
        super(message);
    }
}
