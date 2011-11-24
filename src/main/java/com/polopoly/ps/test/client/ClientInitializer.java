package com.polopoly.ps.test.client;

import com.polopoly.ps.service.Service;
import com.polopoly.ps.service.ServiceGetter;


/**
 * A object that knows how to initialize a {@link ClientContext}. Clients should
 * use the {@link ServiceGetter} to get all implementing classes and ask the to
 * initialize the context.
 */
public interface ClientInitializer extends Service {
    /**
     * Initialize using default parameters.
     */
    ClientContext initialize() throws ClientException;

    /**
     * Initialize using the following string arguments (assumed to have been
     * passed on the command line).
     */
    ClientContext initialize(String[] args) throws ClientException;

    /**
     * Manual initialization. What kind of argument the initializer takes
     * depends on its type.
     */
    ClientContext initialize(Object initializerParameter) throws ClientException;
}
