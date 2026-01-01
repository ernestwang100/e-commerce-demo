package com.superdupermart.shopping.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Incorrect credentials, please try again.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
