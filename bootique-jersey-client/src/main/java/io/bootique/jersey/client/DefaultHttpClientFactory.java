package io.bootique.jersey.client;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import java.security.KeyStore;
import java.util.Map;

/**
 * @since 0.2
 */
public class DefaultHttpClientFactory implements HttpClientFactory {

    @Deprecated
    KeyStore trustStore;
    private ClientConfig config;
    private Map<String, ClientRequestFilter> authFilters;
    private Map<String, KeyStore> trustStores;

    public DefaultHttpClientFactory(
            ClientConfig config,
            // deprecated parameter...
            KeyStore trustStore,
            Map<String, ClientRequestFilter> authFilters,
            Map<String, KeyStore> trustStores) {

        this.authFilters = authFilters;
        this.config = config;
        this.trustStore = trustStore;
        this.trustStores = trustStores;
    }

    @Override
    public HttpClientBuilder newBuilder() {
        ClientBuilder builderDelegate = ClientBuilder.newBuilder().withConfig(config);

        if (trustStore != null) {
            builderDelegate.trustStore(trustStore);
        }

        return new DefaultHttpClientBuilder(builderDelegate);
    }

    private ClientRequestFilter namedAuth(String name) {
        ClientRequestFilter filter = authFilters.get(name);
        if (filter == null) {
            throw new IllegalArgumentException("No authenticator configured for name: " + name);
        }

        return filter;
    }

    private KeyStore namedTrustStore(String name) {
        KeyStore trustStore = trustStores.get(name);
        if (trustStore == null) {
            throw new IllegalArgumentException("No truststore configured for name: " + name);
        }

        return trustStore;
    }

    public class DefaultHttpClientBuilder implements HttpClientBuilder {

        private ClientBuilder delegate;

        public DefaultHttpClientBuilder(ClientBuilder delegate) {
            this.delegate = delegate;
        }

        public Client build() {
            return delegate.build();
        }

        public HttpClientBuilder auth(String authName) {
            delegate.register(namedAuth(authName));
            return this;
        }

        public HttpClientBuilder trustStore(String trustStoreName) {
            delegate.trustStore(namedTrustStore(trustStoreName));
            return this;
        }
    }
}
