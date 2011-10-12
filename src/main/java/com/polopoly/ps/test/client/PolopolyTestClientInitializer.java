package com.polopoly.ps.test.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cache.CacheSettings;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cm.client.ContentCacheSettings;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.util.client.PolopolyClient;

/**
 * Use this for initialization in the integration tests. It configures content
 * filters enabling testing of moderation and deletion of stuff that should not
 * be visible to the front end.
 */
public class PolopolyTestClientInitializer extends PolopolyClientInitializer
		implements ClientInitializer {
	private static final Logger LOGGER = Logger
			.getLogger(PolopolyTestClientInitializer.class.getName());

	private boolean attachSolr = false;
	
	@Override
	protected void configureClient(PolopolyClient client) {
		client.setAttachSolrSearchClient(isAttachSolr());
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

	public void setAttachSolr(boolean attachSolr) {
		this.attachSolr = attachSolr;
	}

	public boolean isAttachSolr() {
		return attachSolr;
	}
}
