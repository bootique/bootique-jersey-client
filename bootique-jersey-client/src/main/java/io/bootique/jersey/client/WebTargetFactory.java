package io.bootique.jersey.client;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;

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
    private String auth;

    // the next block of vars is overriding the values from the parent client config.
    // so they must use objects instead of primitives to maintain a distinction between "null" and "not set".
    private Boolean followRedirectsOverride;
    private Boolean compressionOverride;

    // TODO: other properties...

    @BQConfigProperty
    public void setUrl(URI url) {
        this.url = url;
    }

    @BQConfigProperty("An optional name of the authentication config referencing one of the entries in 'jerseyclient.auth'")
    public void setAuth(String auth) {
        this.auth = auth;
    }

    @BQConfigProperty("If set, overrides the redirect policy of the parent client config set per 'jerseyclient.followRedirects'")
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirectsOverride = followRedirects;
    }

    @BQConfigProperty("If set, overrides the compression policy of the parent client config set per 'jerseyclient.compression'")
    public void setCompression(boolean compression) {
        this.compressionOverride = compression;
    }

    // "compression" is not JAX-RS property, so it is hard to tell whether the parent enabled it or not.
    // The solution here is to accept parent compression as an explicit parameter
    public Supplier<WebTarget> createWebTargetSupplier(HttpClientFactory clientFactory, boolean parentCompression) {

        // copy vars for the supplier (with minimal validation), so that they can not be overridden by the time
        // supplier is executed...

        URI localUrl = Objects.requireNonNull(url, "'url' property is required");
        Boolean followRedirectsOverride = this.followRedirectsOverride;

        // ensure we don't register compression feature twice
        boolean enableCompression = this.compressionOverride != null && this.compressionOverride && !parentCompression;

        // preconfigure client ... it will be the factory for targets
        HttpClientBuilder builder = clientFactory.newBuilder();

        if (auth != null) {
            builder.auth(auth);
        }

        Client client = builder.build();

        return () -> {
            WebTarget target = client.target(localUrl);

            if (followRedirectsOverride != null) {
                target = target.property(ClientProperties.FOLLOW_REDIRECTS, followRedirectsOverride);
            }

            if (enableCompression) {
                target = target.register(new EncodingFeature(GZipEncoder.class));
            }

            return target;
        };
    }
}
