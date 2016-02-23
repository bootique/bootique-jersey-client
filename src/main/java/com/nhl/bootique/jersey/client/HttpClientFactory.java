package com.nhl.bootique.jersey.client;

import javax.ws.rs.client.Client;

public interface HttpClientFactory {

	/**
	 * Returns a new instance of JAX-RS HTTP client preconfigured with the stack
	 * defaults.
	 * 
	 * @return a new instance of JAX-RS HTTP client preconfigured with the stack
	 *         defaults.
	 */
	Client newClient();
}
