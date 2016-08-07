package io.bootique.jersey.client.auth;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.config.PolymorphicConfiguration;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Configuration;

/**
 * @since 0.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AuthenticatorFactory extends PolymorphicConfiguration {

	ClientRequestFilter createAuthFilter(Configuration clientConfig, Injector injector);
}
