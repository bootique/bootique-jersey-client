package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.config.jackson.JsonNodeConfigurationFactory;
import com.nhl.bootique.jackson.DefaultJacksonService;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jersey.client.auth.AuthenticatorFactory;
import com.nhl.bootique.jersey.client.auth.BasicAuthenticatorFactory;
import com.nhl.bootique.log.DefaultBootLogger;

public class HttpClientFactoryFactory_ConfigIT {

	private ConfigurationFactory factory(String yaml) {

		// not using a mock; making sure all Jackson extensions are loaded
		JacksonService jacksonService = new DefaultJacksonService(new DefaultBootLogger(true));

		YAMLParser parser;
		try {
			parser = new YAMLFactory().createParser(yaml);
			JsonNode rootNode = jacksonService.newObjectMapper().readTree(parser);
			return new JsonNodeConfigurationFactory(rootNode, jacksonService.newObjectMapper());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testClientFlags() {
		HttpClientFactoryFactory factory = factory(
				"r:\n  followRedirects: true\n  connectTimeoutMs: 78\n  readTimeoutMs: 66\n  asyncThreadPoolSize: 44\n")
						.config(HttpClientFactoryFactory.class, "r");

		assertEquals(true, factory.followRedirects);
		assertEquals(78, factory.connectTimeoutMs);
		assertEquals(66, factory.readTimeoutMs);
		assertEquals(44, factory.asyncThreadPoolSize);
	}

	@Test
	public void testAuthTypes() {
		HttpClientFactoryFactory factory = factory(
				"r:\n  auth:\n    a1:\n      type: basic\n      username: u1\n      password: p1\n")
						.config(HttpClientFactoryFactory.class, "r");

		assertNotNull(factory.auth);
		assertEquals(1, factory.auth.size());

		AuthenticatorFactory authFactory = factory.auth.get("a1");
		assertNotNull(authFactory);
		assertTrue(authFactory instanceof BasicAuthenticatorFactory);

		BasicAuthenticatorFactory basicAuthFactory = (BasicAuthenticatorFactory) authFactory;
		assertEquals("u1", basicAuthFactory.getUsername());
		assertEquals("p1", basicAuthFactory.getPassword());
	}
}
