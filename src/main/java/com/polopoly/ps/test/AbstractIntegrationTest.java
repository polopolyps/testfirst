package com.polopoly.ps.test;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import com.polopoly.ps.test.client.Client;
import com.polopoly.ps.test.client.ClientContext;
import com.polopoly.ps.test.client.ClientException;
import com.polopoly.ps.test.client.ClientInitializer;
import com.polopoly.ps.test.client.NoSuchServiceException;
import com.polopoly.ps.test.client.PolopolyClientContext;
import com.polopoly.ps.test.client.PolopolyTestClientInitializer;
import com.polopoly.user.server.Caller;

public abstract class AbstractIntegrationTest extends AbstractTest {
	private static final String TEST_CLASS_NAME_SUFFIX = "IntegrationTest";
	protected static PolopolyClientContext context;

	private static Set<Class<? extends AbstractIntegrationTest>> attemptedImports = new HashSet<Class<? extends AbstractIntegrationTest>>();
	private Caller oldCaller;

	private static final Logger LOGGER = Logger
			.getLogger(AbstractIntegrationTest.class.getName());

	@Before
	public void checkClassName() throws Exception {
		if (!getClass().getName().endsWith(TEST_CLASS_NAME_SUFFIX)) {
			Assert.fail("Class name must end with " + TEST_CLASS_NAME_SUFFIX);
		}
	}

	protected static String uri(Content... contents) throws Exception {
		StringBuffer result = new StringBuffer("/cm");

		for (Content content : contents) {
			result.append('/');
			result.append(content.id());
		}

		return result.toString();
	}

	/**
	 * Only use this method when the content of a parent class has to be used
	 * 
	 * @param suffix
	 *            String, has to start with '.' to be valid
	 * @param clazz
	 *            Class<? extends AbstractIntegrationTest>, must be a parent
	 *            class of the executing class to be valid
	 */
	protected Content testContent(String suffix,
			Class<? extends AbstractIntegrationTest> clazz) throws Exception {
		if (!clazz.isAssignableFrom(getClass())) {
			throw new IllegalArgumentException(
					"Cannot use testContent from other class than parent classes (test content should be isolated so it is possible to refactor it without having to consider unknown dependencies.");
		}

		if (!suffix.startsWith(".")) {
			throw new IllegalArgumentException(
					"Suffix "
							+ suffix
							+ " should start with a dot. If it did it would be resolved to the external ID "
							+ getClass().getName() + '.' + suffix + ".");
		}
		if (!clazz.isInstance(this)) {
			throw new IllegalArgumentException("Class "
					+ clazz.getCanonicalName() + " must be a parent class of "
					+ this.getClass().getCanonicalName());
		}

		return new Content(clazz, suffix);
	}

	protected Content testContent(String suffix) throws Exception {
		return testContent(suffix, this.getClass());
	}

	@Override
	@Before
	public final void setUpClient() throws Exception {
		super.setUpClient();

		context = PolopolyClientContext
				.toPolopolyContext(getDefaultClientContext());

		importTestContent();

		oldCaller = context.getPolicyCMServer().getCurrentCaller();

		if (oldCaller == null || oldCaller.getUserId() == null
				|| !"98".equals(oldCaller.getUserId().getPrincipalIdString())) {
			LOGGER.log(
					Level.WARNING,
					"Expected the caller to have syadmin logged in before tests started, but the caller was "
							+ oldCaller + ".");
		}

	}

	protected ClientContext getDefaultClientContext() throws ClientException {
		return Client.getContext();
	}

	@Override
	@After
	public final void tearDownClient() {
		super.tearDownClient();

		context.getPolicyCMServer().setCurrentCaller(oldCaller);
	}

	@Override
	protected ClientInitializer getInitializer() throws NoSuchServiceException {
		return new PolopolyTestClientInitializer();
	}

	public void importTestContent() {
		if (attemptedImports.contains(getClass())) {
			return;
		}

		attemptedImports.add(getClass());

		new ContentImporter().importTestContent(getClass(), context);
	}

	public PolopolyClientContext getContext() {
		return context;
	}

	static {
		// hard-coding this; for some reason setting it through a property in
		// maven doesn't seem to work on CI.
		Logger.getLogger("com.polopoly.").setLevel(Level.WARNING);
	}
}
