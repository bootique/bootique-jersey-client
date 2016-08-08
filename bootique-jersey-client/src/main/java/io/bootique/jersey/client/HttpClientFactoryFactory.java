package io.bootique.jersey.client;

import com.google.inject.Injector;
import io.bootique.jersey.client.auth.AuthenticatorFactory;
import io.bootique.jersey.client.log.JULSlf4jLogger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpClientFactoryFactory {

    boolean followRedirects;
    boolean compression;
    int readTimeoutMs;
    int connectTimeoutMs;
    int asyncThreadPoolSize;
    Map<String, AuthenticatorFactory> auth;

    public HttpClientFactoryFactory() {
        this.compression = true;
    }

    /**
     * @param auth a map of AuthenticationFactory instances by symbolic name.
     * @since 0.2
     */
    public void setAuth(Map<String, AuthenticatorFactory> auth) {
        this.auth = auth;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public void setAsyncThreadPoolSize(int asyncThreadPoolSize) {
        this.asyncThreadPoolSize = asyncThreadPoolSize;
    }

    /**
     * Enables or disables client-side compression headers. True by default.
     *
     * @param compression whether compression should be requested.
     * @since 0.6
     */
    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    public HttpClientFactory createClientFactory(Injector injector, Set<Feature> features) {
        ClientConfig config = createConfig(features);

        // register Guice Injector as a service in Jersey HK2, and
        // GuiceBridgeFeature as a
        ClientGuiceBridgeFeature.register(config, injector);

        return new DefaultHttpClientFactory(config, createAuthFilters(config, injector));
    }

    protected Map<String, ClientRequestFilter> createAuthFilters(Configuration clientConfig, Injector injector) {
        Map<String, ClientRequestFilter> filters = new HashMap<>();

        if (auth != null) {
            auth.forEach((k, v) -> filters.put(k, v.createAuthFilter(clientConfig, injector)));
        }

        return filters;
    }

    protected ClientConfig createConfig(Set<Feature> features) {
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.FOLLOW_REDIRECTS, followRedirects);
        config.property(ClientProperties.READ_TIMEOUT, readTimeoutMs);
        config.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutMs);
        config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, asyncThreadPoolSize);

        features.forEach(f -> config.register(f));

        if(compression) {
            config.register(new EncodingFeature(GZipEncoder.class));
        }

        configRequestLogging(config);

        return config;
    }

    protected void configRequestLogging(ClientConfig config) {

        Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);
        if (logger.isDebugEnabled()) {

            JULSlf4jLogger julWrapper = new JULSlf4jLogger(HttpClientFactoryFactory.class.getName(), logger);
            LoggingFilter logFilter = new LoggingFilter(julWrapper, false);
            config.register(logFilter);
        }
    }
}
