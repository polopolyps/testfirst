package com.polopoly.ps.test;

import com.polopoly.ps.test.client.PolopolyClientContext;
import com.polopoly.util.CheckedCast;
import com.polopoly.util.contentid.ContentIdUtil;
import com.polopoly.util.policy.PolicyUtilImpl;

public class Content extends PolicyUtilImpl {
    private Class<? extends AbstractIntegrationTest> klass;
    private String suffix;

    public Content(Class<? extends AbstractIntegrationTest> klass, String suffix) throws Exception {
        super(AbstractIntegrationTest.context.getPolicy(externalId(klass, suffix)), AbstractIntegrationTest.context);

        this.klass = klass;
        this.suffix = suffix;
    }

    private PolopolyClientContext context() {
        return AbstractIntegrationTest.context;
    }

    public ContentIdUtil id() throws Exception {
        return context().resolveExternalId(externalId());
    }

    private String externalId() {
        return externalId(klass, suffix);
    }

    private static String externalId(Class<? extends AbstractIntegrationTest> klass, String suffix) {
        return klass.getName() + suffix;
    }

    public <T> T asPolicy(Class<T> clazz) throws Exception {
        return CheckedCast.cast(asPolicy(), clazz, "Policy of " + suffix);
    }
}
