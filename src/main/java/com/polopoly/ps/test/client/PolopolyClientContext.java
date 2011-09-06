package com.polopoly.ps.test.client;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Caller;
import com.polopoly.util.client.PolopolyContext;

/**
 * Extends the PCMD {@link PolopolyContext} to become a {@link ClientContext}.
 * This is the main {@link ClientContext} used in the system. If you really need
 * to sidestep the model object APIs to access Polopoly APIs directly do it by:
 * 
 * <pre>
 * PolopolyClientContext.toPolopolyContext(Client.getContext())
 * </pre>
 * 
 * @see Client
 */
public class PolopolyClientContext extends PolopolyContext implements ClientContext {
    private Caller caller;

    public static PolopolyClientContext toPolopolyContext(ClientContext context) {
        if (!(context instanceof PolopolyClientContext)) {
            throw new IllegalArgumentException("Expected a Polopoly context rather than "
                    + (context == null ? "null" : context.getClass().getName()) + ".");
        }

        return (PolopolyClientContext) context;
    }

    public PolopolyClientContext(PolopolyContext context) {
        super(context);

        this.caller = getPolicyCMServer().getCurrentCaller();
    }

    public PolopolyClientContext(PolicyCMServer server) {
        super(server);

        this.caller = getPolicyCMServer().getCurrentCaller();
    }

    public void initializeThread() {
        getPolicyCMServer().setCurrentCaller(caller);
    }

    @Override
    public String toString() {
        return getApplication().getName();
    }

    public String getApplicationName() {
        return getApplication().getName();
    }
    }
