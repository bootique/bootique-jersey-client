package io.bootique.jersey.client;

import javax.ws.rs.client.Client;

/**
 * JAX RS {@link javax.ws.rs.client.Client} builder that builds the client with references to various parts of
 * "jerseyclient" Bootique configuration.
 *
 * @since 0.25
 */
public interface HttpClientBuilder {

    Client build();

    HttpClientBuilder auth(String authName);

    HttpClientBuilder trustStore(String trustStoreName);
}
