package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.Test;

public class HttpClientFactoryFactoryTest {

	@Test
	public void testCreateClientFactory() {
		
		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setAsyncThreadPoolSize(5);
		factoryFactory.setConnectTimeoutMs(101);
		factoryFactory.setFollowRedirects(true);
		factoryFactory.setReadTimeoutMs(203);

		HttpClientFactory factory = factoryFactory.createClientFactory(Collections.emptySet());
		assertNotNull(factory);

		Client client = factory.newClient();

		try {

			assertEquals(5, client.getConfiguration().getProperty(ClientProperties.ASYNC_THREADPOOL_SIZE));
			assertEquals(101, client.getConfiguration().getProperty(ClientProperties.CONNECT_TIMEOUT));
			assertEquals(true, client.getConfiguration().getProperty(ClientProperties.FOLLOW_REDIRECTS));
			assertEquals(203, client.getConfiguration().getProperty(ClientProperties.READ_TIMEOUT));

		} finally {
			client.close();
		}
	}
}
