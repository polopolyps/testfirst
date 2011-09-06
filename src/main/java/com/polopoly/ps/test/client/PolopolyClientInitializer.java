package com.polopoly.ps.test.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.util.CheckedCast;
import com.polopoly.util.CheckedClassCastException;
import com.polopoly.util.client.ConnectException;
import com.polopoly.util.client.PolopolyClient;
import com.polopoly.util.client.PolopolyContext;

/**
 * The {@link ClientInitializer} that initializes a
 * {@link PolopolyClientContext}.
 */
public class PolopolyClientInitializer implements ClientInitializer {
	private static final Logger logger = Logger
			.getLogger(PolopolyClientInitializer.class.getName());
	private PolopolyClientContext initializedContext;

	public ClientContext initialize(String[] args) throws ClientException {
		return initialize();
	}

	public ClientContext initialize() throws ClientException {
		if (initializedContext != null) {
			logger.log(Level.WARNING, "Attempt to initialized twice.");

			return initializedContext;
		}

		PolopolyClient client = createClient();

		client.setAttachSearchService(true);
		client.setAttachStatisticsService(true);
		client.setAttachPollService(true);

		client.setAttachLRUSynchronizedUpdateCache(true);
		client.setAttachSolrSearchClient(true);

		configureClient(client);

		try {
			initializedContext = new PolopolyClientContext(client.connect());

			return initializedContext;
		} catch (ConnectException e) {
			throw new ClientException(e);
		}
	}

	protected PolopolyClient createClient() {
		return new PolopolyClient();
	}

	protected void configureClient(PolopolyClient client) {
		// intended for overwriting
	}

	public ClientContext initialize(Object initializerParameter)
			throws ClientException {
		try {
			PolopolyContext context = CheckedCast.cast(initializerParameter,
					PolopolyContext.class, "Initializer parameter");

			return new PolopolyClientContext(context);
		} catch (CheckedClassCastException e) {
			throw new ClientException(e);
		}
	}
}
