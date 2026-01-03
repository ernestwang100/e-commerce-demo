package com.superdupermart.shopping.exception;

public class NotEnoughInventoryException extends RuntimeException {
    public NotEnoughInventoryException(String message) {
        super(message);
    }
}
