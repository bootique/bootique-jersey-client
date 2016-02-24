package com.nhl.bootique.jersey.client;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;

import org.glassfish.jersey.client.ClientConfig;

/**
 * @since 0.2
 */
public class DefaultHttpClientFactory implements HttpClientFactory {

	private ClientConfig config;
	private Map<String, ClientRequestFilter> authFilters;

	public DefaultHttpClientFactory(ClientConfig config, Map<String, ClientRequestFilter> authFilters) {
		this.authFilters = authFilters;
		this.config = config;
	}

	@Override
	public Client newClient() {
		return ClientBuilder.newClient(config);
	}

	@Override
	public Client newAuthenticatedClient(String authName) {

		ClientRequestFilter filter = authFilters.get(authName);
		if (filter == null) {
			throw new IllegalArgumentException("No authentication for name: " + authName);
		}

		return newClient().register(filter);
	}

}
