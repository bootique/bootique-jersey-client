package io.bootique.jersey.client;

import javax.ws.rs.client.Client;

import io.bootique.jersey.client.auth.AuthenticatorFactory;

public interface HttpClientFactory {

	/**
	 * Returns a new instance of JAX-RS HTTP client initialized using
	 * "jerseyclient" configuration subtree.
	 * 
	 * @return a new instance of JAX-RS HTTP client initialized using
	 *         "jerseyclient" configuration subtree.
	 */
	Client newClient();

	/**
	 * Returns a new instance of JAX-RS HTTP client initialized using
	 * "jerseyclient" configuration subtree and associated with named
	 * authenticator.
	 * 
	 * @return a new instance of JAX-RS HTTP client initialized using
	 *         "jerseyclient" configuration subtree and associated with named
	 *         authenticator.
	 * 
	 * @since 0.2
	 * @see AuthenticatorFactory
	 */
	Client newAuthenticatedClient(String authName);
}
