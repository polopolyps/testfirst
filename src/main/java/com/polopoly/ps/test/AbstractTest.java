package com.polopoly.ps.test;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import com.polopoly.ps.test.client.Client;
import com.polopoly.ps.test.client.ClientContext;
import com.polopoly.ps.test.client.ClientInitializer;
import com.polopoly.ps.test.service.ServiceGetter;
import com.polopoly.ps.test.service.ServiceGetter.ServicesBackup;

public abstract class AbstractTest {
	protected static boolean initialized;
	protected ClientContext context;
	private ServicesBackup servicesBackup;

	protected static Random random = new Random();

	@Before
	public void setUpClient() throws Exception {
		if (!initialized) {
			context = Client.initializeDefaultClient(ServiceGetter.wrap(
					getInitializer(), ClientInitializer.class));
			initialized = true;
		} else {
			context = Client.getContext();
		}

		servicesBackup = ServiceGetter.backup();
	}

	@After
	public void tearDownClient() {
		if (servicesBackup != null) {
			servicesBackup.restore();
		}
	}

	protected abstract ClientInitializer getInitializer() throws Exception;

	protected <T> void assertDoesNotContain(Iterator<T> iterator, T object) {
		Assert.assertFalse("Did not expect " + object + " in iterator.",
				contains(iterator, object));
	}

	protected <T> void assertContains(Iterator<T> iterator, T object) {
		Assert.assertTrue("Expected " + object + " in iterator.",
				contains(iterator, object));
	}

	private <T> boolean contains(Iterator<T> iterator, T object) {
		while (iterator.hasNext()) {
			T next = iterator.next();

			if (next.equals(object)) {
				return true;
			}
		}

		return false;
	}

	protected void assertContains(String string, String mustBeContained) {
		Assert.assertTrue("Should contains: " + mustBeContained + " but was: "
				+ string, string.contains(mustBeContained));
	}

	protected <T> void assertContains(T[] array, T object) {
		for (T arrayElement : array) {
			if (arrayElement.equals(object)) {
				return;
			}
		}

		Assert.fail("The array " + Arrays.toString(array) + " did not contain "
				+ object + ".");
	}

	protected <T> void assertIterableEquals(Iterable<T> iterable1,
			Iterable<T> iterable2) {
		assertIteratorEquals(iterable1.iterator(), iterable2.iterator());
	}

	protected <T> void assertIteratorEquals(Iterator<T> iterator1,
			Iterator<T> iterator2) {
		while (iterator1.hasNext()) {
			Assert.assertTrue(iterator2.hasNext());
			Assert.assertEquals(iterator1.next(), iterator2.next());
		}

		Assert.assertFalse(iterator2.hasNext());
	}

	protected Date now() {
		return new Date();
	}

	protected Date yearsAgo() {
		return new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365
				* 2);
	}

	protected static String createRandomString() {
		String longNumber = Long.toString(Math.abs(random.nextLong()));
		char[] randomCharacters = longNumber.toCharArray();
		for (int i = 0; i < randomCharacters.length; i++) {
			randomCharacters[i] = Character.toChars(Character.codePointAt(
					randomCharacters, i) + 17)[0];
		}

		return String.valueOf(randomCharacters);
	}
}
