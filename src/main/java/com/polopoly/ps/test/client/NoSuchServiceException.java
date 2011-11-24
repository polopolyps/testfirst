package com.polopoly.ps.test.client;

/**
 * @deprecated This class has moved to {@link com.polopoly.ps.service.NoSuchServiceException} in module pcmd. 
 * 
 */
@SuppressWarnings("serial")
@Deprecated
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
