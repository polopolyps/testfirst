package com.polopoly.ps.test;

/**
 * @see ServletRequestThreadLocal#getRequest()
 */
public class NoCurrentRequestException extends Exception {

    public NoCurrentRequestException() {
        super();
    }

    public NoCurrentRequestException(Throwable cause) {
        super(cause);
    }

}
