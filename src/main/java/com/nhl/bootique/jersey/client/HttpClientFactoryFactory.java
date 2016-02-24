package com.nhl.bootique.jersey.client;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.ClientRequestFilter;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.nhl.bootique.jersey.client.auth.AuthenticatorFactory;

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

	public HttpClientFactory createClientFactory() {
		return new DefaultHttpClientFactory(createConfig(), createAuthFilters());
	}

	protected Map<String, ClientRequestFilter> createAuthFilters() {
		Map<String, ClientRequestFilter> filters = new HashMap<>();

		if (auth != null) {
			auth.forEach((k, v) -> filters.put(k, v.createAuthFilter()));
		}

		return filters;
	}

	protected ClientConfig createConfig() {
		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.FOLLOW_REDIRECTS, followRedirects);
		config.property(ClientProperties.READ_TIMEOUT, readTimeoutMs);
		config.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutMs);
		config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, asyncThreadPoolSize);
		return config;
	}
}
