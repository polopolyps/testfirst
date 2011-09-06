package com.polopoly.ps.test.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cache.CacheSettings;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentCacheSettings;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.util.client.PolopolyClient;
import com.polopoly.util.client.PolopolyContext;
import com.polopoly.util.exception.PolicyCreateException;
import com.polopoly.util.policy.Util;

/**
 * Use this for initialization in the integration tests. It configures content
 * filters enabling testing of moderation and deletion of stuff that should not
 * be visible to the front end.
 */
public class PolopolyTestClientInitializer extends PolopolyClientInitializer
		implements ClientInitializer {
	private static final Logger LOGGER = Logger
			.getLogger(PolopolyTestClientInitializer.class.getName());

	private static Policy fakeSite;

	@Override
	protected void configureClient(PolopolyClient client) {
		client.setAttachSolrSearchClient(true);
	}

	@Override
	protected PolopolyClient createClient() {
		return new PolopolyClient() {
			@Override
			protected void setUpLRUSynchronizedUpdateCache(
					LRUSynchronizedUpdateCache cache) {
				super.setUpLRUSynchronizedUpdateCache(cache);

				try {
					CacheSettings cacheSettings = new CacheSettings();
					cacheSettings.setMaxSize(0);
					cache.setCacheSettings(cacheSettings);
				} catch (IllegalApplicationStateException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
				}
			}

			@Override
			protected void setUpCmClient(EjbCmClient cmClient) {
				super.setUpCmClient(cmClient);

				try {
					ContentCacheSettings settings = new ContentCacheSettings();
					settings.setPolicyMemoryCacheSize(512);
					settings.setContentMemoryCacheSize(2048);
					cmClient.setContentCacheSettings(settings);
				} catch (IllegalApplicationStateException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		};
	}

	@Override
	public ClientContext initialize() throws ClientException {
		ClientContext result = super.initialize();

		return result;
	}

	protected Policy getFakeSite(PolopolyContext context)
			throws PolicyCreateException {
		if (fakeSite == null) {
			try {
				fakeSite = context.createPolicy(2, "p.siteengine.Site");
				fakeSite.getContent().commit();

				LOGGER.log(
						Level.INFO,
						"There was no site finder. Created fake site "
								+ Util.util(fakeSite) + " and returned it.");

			} catch (CMException e) {
				throw new PolicyCreateException("Cannot create fake site", e);
			}
		}

		return fakeSite;
	}
}
