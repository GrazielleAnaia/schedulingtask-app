package com.grazielleanaia.scheduling_api.infrastructure.exception;

public class CustomerServiceUnavailableException extends RuntimeException {
    public CustomerServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerServiceUnavailableException(String message) {
        super(message);
    }
}
