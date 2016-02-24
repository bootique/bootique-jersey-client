package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.config.YamlConfigurationFactory;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jersey.client.auth.AuthenticatorFactory;
import com.nhl.bootique.jersey.client.auth.BasicAuthenticatorFactory;

public class HttpClientFactoryFactory_ConfigIT {

	private ConfigurationSource mockConfigSource;
	private JacksonService mockJacksonService;
	private Environment mockEnvironment;

	@Before
	public void before() {
		mockConfigSource = mock(ConfigurationSource.class);
		mockJacksonService = mock(JacksonService.class);
		when(mockJacksonService.newObjectMapper()).thenReturn(new ObjectMapper());

		mockEnvironment = mock(Environment.class);
	}

	private YamlConfigurationFactory factory(String yaml) {
		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream(yaml.getBytes());

			return processor.apply(in);
		});

		return new YamlConfigurationFactory(mockConfigSource, mockEnvironment, mockJacksonService);
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
