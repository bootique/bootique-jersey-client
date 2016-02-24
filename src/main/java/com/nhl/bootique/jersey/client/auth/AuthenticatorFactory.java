package com.nhl.bootique.jersey.client.auth;

import javax.ws.rs.client.ClientRequestFilter;

/**
 * @since 0.2
 */
public interface AuthenticatorFactory {

	ClientRequestFilter createAuthFilter();
}
