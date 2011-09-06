package com.polopoly.ps.test;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a way of storing the current servlet request in a ThreadLocal
 * variable so Linkable implementations are able to fetch it (and therefore from
 * it fetch the current URLBuilder Polopoly has configured) without having to
 * depend on servlet classes.
 */
public class ServletRequestThreadLocal {
    private static ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>();

    public static void storeRequest(HttpServletRequest request) {
        requests.set(request);
    }

    @Deprecated
    /**
     * Avoid using this method unless there is absolutely no other way of achieving what you want.
     */
    public static HttpServletRequest getRequest() throws NoCurrentRequestException {
        HttpServletRequest result = requests.get();

        if (result == null) {
            throw new NoCurrentRequestException();
        }

        return result;
    }

    static void clearRequest() {
        requests.set(null);
    }
}
