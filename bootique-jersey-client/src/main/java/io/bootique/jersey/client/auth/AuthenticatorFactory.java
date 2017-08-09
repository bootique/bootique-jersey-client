package io.bootique.jersey.client.auth;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.config.PolymorphicConfiguration;

import javax.ws.rs.client.ClientRequestFilter;

/**
 * @since 0.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@BQConfig("Authenticator for a given auth protocol.")
public interface AuthenticatorFactory extends PolymorphicConfiguration {

    /**
     * @param injector DI injector that can be used to lookup extra services required by the factory.
     * @return
     * @since 0.24
     */
    ClientRequestFilter createAuthFilter(Injector injector);
}
