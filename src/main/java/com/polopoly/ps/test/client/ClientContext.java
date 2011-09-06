package com.polopoly.ps.test.client;

/**
 * Returned by {@link Client#getContext()} and encapsulates the Polopoly
 * singletons such as the PolicyCMServer.
 */
public interface ClientContext {
    /**
     * Call this method first thing if you start a new thread from your code
     * (since otherwise it has no Polopoly caller).
     */
    void initializeThread();

    /**
     * Returns "preview" in the preview application, "polopoly" in the GUI or
     * "front" in the front (or an undefined value if not using a Polopoly
     * client).
     */
    String getApplicationName();
}
