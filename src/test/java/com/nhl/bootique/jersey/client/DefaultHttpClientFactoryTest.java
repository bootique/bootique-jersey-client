package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Test;

public class DefaultHttpClientFactoryTest {

	private ClientConfig config;
	private ClientRequestFilter mockAuth1;
	private ClientRequestFilter mockAuth2;

	@Before
	public void before() {
		config = new ClientConfig();
		mockAuth1 = mock(ClientRequestFilter.class);
		mockAuth2 = mock(ClientRequestFilter.class);
	}

	@Test
	public void testNewClient() {

		config.property("x", "y");

		DefaultHttpClientFactory factory = new DefaultHttpClientFactory(config, Collections.emptyMap());
		Client c = factory.newClient();
		assertNotNull(c);

		assertEquals("y", c.getConfiguration().getProperty("x"));
	}

	@Test
	public void testNewClient_Auth() {

		config.property("a", "b");

		Map<String, ClientRequestFilter> authFilters = new HashMap<>();
		authFilters.put("one", mockAuth1);
		authFilters.put("two", mockAuth2);

		DefaultHttpClientFactory factory = new DefaultHttpClientFactory(config, authFilters);
		Client c = factory.newAuthenticatedClient("one");
		assertNotNull(c);

		assertEquals("b", c.getConfiguration().getProperty("a"));
		assertTrue(c.getConfiguration().isRegistered(mockAuth1));
		assertFalse(c.getConfiguration().isRegistered(mockAuth2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewClient_Auth_BadAuth() {

		config.property("a", "b");

		Map<String, ClientRequestFilter> authFilters = new HashMap<>();
		authFilters.put("one", mockAuth1);
		authFilters.put("two", mockAuth2);

		DefaultHttpClientFactory factory = new DefaultHttpClientFactory(config, authFilters);
		factory.newAuthenticatedClient("three");
	}
}
