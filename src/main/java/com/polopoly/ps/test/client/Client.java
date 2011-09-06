package com.polopoly.ps.test.client;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A client using model objects. Before starting to use model objects, the
 * client must be initialized (once) using this class. The abstracts away the
 * connecting to Polopoly for implementation-independent code.
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private static ClientContext context;

    private static boolean initialized;

    /**
     * Do a default initialization. In the Polopoly implementation this
     * corresponds to connecting to the locally running Polopoly instance.
     */
    public static void initializeDefaultClient() throws ClientException {
        if (initialized) {
            if (context != null) {
                LOGGER.log(Level.WARNING, "Attempt to initialize client twice.");

                return;
            } else {
                throw new ClientException("Initialization failed at a previous attempt.");
            }
        }

        ClientInitializer initializer = preInitialization();

        initializeDefaultClient(initializer);
    }

    public static ClientContext initializeDefaultClient(ClientInitializer initializer) throws ClientException {
        if (initialized) {
            if (context != null) {
                LOGGER.log(Level.WARNING, "Attempt to initialize client twice.");

                return context;
            } else {
                throw new ClientException("Initialization failed at a previous attempt.");
            }
        }

        context = initializer.initialize();
        initialized = true;

        return context;
    }

    /**
     * Do a manual initialization after manually creating the client. This is
     * only used in the Polopoly case, where a web client already has access to
     * a PolopolyContext and the client must reuse it.
     * 
     * @param initializerParameter
     *            The type of this object depends on the initializer since it is
     *            passed on to it.
     */
    public static ClientContext initializeManually(Object initializerParameter) throws ClientException {
        ClientInitializer initializer = preInitialization();

        return initializeManually(initializer, initializerParameter);
    }

    public static ClientContext initializeManually(ClientInitializer initializer, Object initializerParameter)
            throws ClientException {
        if (context != null) {
            throw new ClientException("Attempt to initialize command line client twice.");
        }

        context = initializer.initialize(initializerParameter);

        return context;
    }

    public static ClientContext initializeCommandLineClient(String[] args) throws ClientException {
        ClientInitializer initializer = preInitialization();

        context = initializer.initialize(args);

        return context;
    }

    private static ClientInitializer preInitialization() throws ClientException {
        return new PolopolyClientInitializer();
    }

    public static ClientContext getContext() throws ContextNotInitializedException {
        if (context == null) {
            throw new ContextNotInitializedException("initialize must be called before fetching context.");
        }

        return context;
    }

    public static boolean isInitialized() {
        return context != null;
    }
}
