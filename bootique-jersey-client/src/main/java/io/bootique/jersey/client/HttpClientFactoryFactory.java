package io.bootique.jersey.client;

import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.auth.AuthenticatorFactory;
import io.bootique.jersey.client.log.RequestLoggingFilter;
import io.bootique.jersey.client.log.JULSlf4jLogger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@BQConfig("Configures HttpClientFactory, including named authenticators, timeouts, SSL certificates, etc.")
public class HttpClientFactoryFactory {

    boolean followRedirects;
    boolean compression;
    int readTimeoutMs;
    int connectTimeoutMs;
    int asyncThreadPoolSize;
    Map<String, AuthenticatorFactory> auth;
    Map<String, TrustStoreFactory> trustStores;
    Map<String, WebTargetFactory> targets;

    public HttpClientFactoryFactory() {
        this.followRedirects = true;
        this.compression = true;
    }

    /**
     * @param auth a map of AuthenticationFactory instances by symbolic name.
     * @since 0.2
     */
    @BQConfigProperty("A map of named \"authenticators\" for HTTP services that require authentication.")
    public void setAuth(Map<String, AuthenticatorFactory> auth) {
        this.auth = auth;
    }

    @BQConfigProperty("Sets whether the client should autromatically follow redirects. The default is 'true'.")
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    @BQConfigProperty("Sets the read timeout. The default (0) means no timeout.")
    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    @BQConfigProperty("Sets the connect timeout. The default (0) means no timeout.")
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    @BQConfigProperty("Sets the size of the async requests thread pool. The default (0) sets no limit on the pool.")
    public void setAsyncThreadPoolSize(int asyncThreadPoolSize) {
        this.asyncThreadPoolSize = asyncThreadPoolSize;
    }

    /**
     * Sets a map of named client trust store factories.
     *
     * @param trustStores a map of named trust store factories.
     * @since 0.25
     */
    @BQConfigProperty
    public void setTrustStores(Map<String, TrustStoreFactory> trustStores) {
        this.trustStores = trustStores;
    }

    /**
     * Enables or disables client-side compression headers. True by default.
     *
     * @param compression whether compression should be requested.
     * @since 0.6
     */
    @BQConfigProperty
    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    /**
     * Sets a map of named target factories. This allows to define remote endpoints completely via configuration.
     *
     * @param targets a map of named target factories.
     * @since 0.25
     */
    @BQConfigProperty
    public void setTargets(Map<String, WebTargetFactory> targets) {
        this.targets = targets;
    }

    /**
     * Creates and returns a new HttpTargetFactory for the  set of targets preconfigured in this factory.
     *
     * @param clientFactory
     * @return a new HttpTargetFactory for the preconfigured set of targets.
     * @since 0.25
     */
    public HttpTargets createTargets(HttpClientFactory clientFactory) {
        return new DefaultHttpTargets(createNamedTargets(clientFactory));
    }

    public HttpClientFactory createClientFactory(Injector injector, Set<Feature> features) {
        ClientConfig config = createConfig(features);

        // register Guice Injector as a service in Jersey HK2, and GuiceBridgeFeature as a client Feature
        ClientGuiceBridgeFeature.register(config, injector);

        return new DefaultHttpClientFactory(
                config,
                createAuthFilters(injector),
                createTrustStores());
    }

    protected Map<String, KeyStore> createTrustStores() {

        if (trustStores == null) {
            return Collections.emptyMap();
        }

        Map<String, KeyStore> keyStores = new HashMap<>();
        trustStores.forEach((k, v) -> keyStores.put(k, v.createTrustStore()));
        return keyStores;
    }

    protected Map<String, ClientRequestFilter> createAuthFilters(Injector injector) {
        Map<String, ClientRequestFilter> filters = new HashMap<>();

        if (auth != null) {
            auth.forEach((k, v) -> filters.put(k, v.createAuthFilter(injector)));
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

        if (compression) {
            config.register(new EncodingFeature(GZipEncoder.class));
        }

        configRequestLogging(config);

        return config;
    }

    protected void configRequestLogging(ClientConfig config) {

        Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);
        if (logger.isDebugEnabled()) {
            JULSlf4jLogger julWrapper = new JULSlf4jLogger(HttpClientFactoryFactory.class.getName(), logger);
            RequestLoggingFilter logFilter = new RequestLoggingFilter(julWrapper);
            config.register(logFilter);
        }
    }

    protected Map<String, Supplier<WebTarget>> createNamedTargets(HttpClientFactory clientFactory) {
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Supplier<WebTarget>> suppliers = new HashMap<>();
        targets.forEach((n, f) -> suppliers.put(n, f.createWebTargetSupplier(clientFactory, compression)));

        return suppliers;
    }
}
