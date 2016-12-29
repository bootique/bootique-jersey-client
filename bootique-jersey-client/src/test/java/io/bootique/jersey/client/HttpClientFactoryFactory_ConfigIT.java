package io.bootique.jersey.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.config.jackson.JsonNodeConfigurationFactory;
import io.bootique.jackson.DefaultJacksonService;
import io.bootique.jackson.ImmutableSubtypeResolver;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.client.auth.AuthenticatorFactory;
import io.bootique.jersey.client.auth.BasicAuthenticatorFactory;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpClientFactoryFactory_ConfigIT {

    private BootLogger bootLogger;
    private TypesFactory<PolymorphicConfiguration> typesFactory;

    @Before
    public void before() {
        bootLogger = new DefaultBootLogger(true);
        typesFactory = new TypesFactory<>(getClass().getClassLoader(), PolymorphicConfiguration.class, bootLogger);
    }

	private ConfigurationFactory factory(String yaml) {

		// not using a mock; making sure all Jackson extensions are loaded
		JacksonService jacksonService = new DefaultJacksonService(new ImmutableSubtypeResolver(typesFactory.getTypes()), bootLogger);

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
