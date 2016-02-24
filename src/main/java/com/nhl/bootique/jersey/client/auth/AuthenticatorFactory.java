package com.nhl.bootique.jersey.client.auth;

import javax.ws.rs.client.ClientRequestFilter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @since 0.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")

// TODO: switch to dynamic mechanism per
// https://github.com/nhl/bootique/issues/14
@JsonSubTypes(value = { @JsonSubTypes.Type(value = BasicAuthenticatorFactory.class) })
public interface AuthenticatorFactory {

	ClientRequestFilter createAuthFilter();
}
