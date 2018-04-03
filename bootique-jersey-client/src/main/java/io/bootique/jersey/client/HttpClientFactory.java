package io.bootique.jersey.client;

import javax.ws.rs.client.Client;

/**
 * An injectable factory for creating JAX RS clients based on Bootique configuration.
 */
public interface HttpClientFactory {

    /**
     * Returns a new instance of JAX-RS HTTP {@link Client} initialized using "jerseyclient" configuration subtree.
     *
     * @return a new instance of JAX-RS HTTP client initialized using
     * "jerseyclient" configuration subtree.
     */
    default Client newClient() {
        return newBuilder().build();
    }

    /**
     * A builder for a new client. Allows to create a client with Bootique configuration-driven settings and select
     * a preconfigured authentication and trust store.
     *
     * @return a builder for a new client.
     * @since 0.25
     */
    HttpClientBuilder newBuilder();
}
