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

    private ClientConfig config;
    private Map<String, ClientRequestFilter> authFilters;
    private KeyStore trustStore;

    public DefaultHttpClientFactory(ClientConfig config, KeyStore trustStore, Map<String, ClientRequestFilter> authFilters) {
        this.authFilters = authFilters;
        this.config = config;
        this.trustStore = trustStore;
    }

    @Override
    public Client newClient() {

        ClientBuilder builder = ClientBuilder.newBuilder().withConfig(config);

        if (trustStore != null) {
            builder = builder.trustStore(trustStore);
        }

        return builder.build();
    }

    @Override
    public Client newAuthenticatedClient(String authName) {

        ClientRequestFilter filter = authFilters.get(authName);
        if (filter == null) {
            throw new IllegalArgumentException("No authenticator configured for name: " + authName);
        }

        return newClient().register(filter);
    }

}
