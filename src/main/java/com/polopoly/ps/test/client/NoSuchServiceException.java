package com.polopoly.ps.test.client;

public class NoSuchServiceException extends Exception {

    public NoSuchServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchServiceException(String message) {
        super(message);
    }

    public NoSuchServiceException(Throwable cause) {
        super(cause);
    }

}
