package com.nhl.bootique.jersey.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.nhl.bootique.jersey.client.auth.AuthenticatorFactory;
import com.nhl.bootique.jersey.client.log.JULSlf4jLogger;

public class HttpClientFactoryFactory {

	boolean followRedirects;
	int readTimeoutMs;
	int connectTimeoutMs;
	int asyncThreadPoolSize;
	Map<String, AuthenticatorFactory> auth;

	/**
	 * @since 0.2
	 * @param auth
	 *            a map of AuthenticationFactory instances by symbolic name.
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

		configRequestLogging(config);

		return config;
	}

	protected void configRequestLogging(ClientConfig config) {

		Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);
		if (logger.isDebugEnabled()) {

			JULSlf4jLogger julWrapper = new JULSlf4jLogger(HttpClientFactory.class.getName(), logger);
			LoggingFilter logFilter = new LoggingFilter(julWrapper, false);
			config.register(logFilter);
		}
	}
}
