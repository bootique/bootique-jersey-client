package io.bootique.jersey.client.targets;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.HttpClientFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @since 0.25
 */
@BQConfig
public class WebTargetFactory {

    private URI url;

    // TODO: other properties...

    @BQConfigProperty
    public void setUrl(URI url) {
        this.url = url;
    }

    public Supplier<WebTarget> createWebTargetSupplier(HttpClientFactory clientFactory) {

        // validate and copy vars for the supplier, so that they can not be overridden by the time supplier is
        // executed...

        URI localUrl = Objects.requireNonNull(url, "'url' property is required");

        // preconfigure client ... it will be the factory for targets
        Client client = clientFactory.newClient();

        return () -> client.target(localUrl);
    }
}
