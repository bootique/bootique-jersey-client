package com.nhl.bootique.jersey.client.auth;

import javax.ws.rs.client.ClientRequestFilter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhl.bootique.config.PolymorphicConfiguration;

/**
 * @since 0.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AuthenticatorFactory extends PolymorphicConfiguration{

	ClientRequestFilter createAuthFilter();
}
