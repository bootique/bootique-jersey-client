package com.nhl.bootique.jersey.client;

import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public class HttpClientFactoryFactory {

	private boolean followRedirects;
	private int readTimeoutMs;
	private int connectTimeoutMs;
	private int asyncThreadPoolSize;

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
		ClientConfig config = new ClientConfig();
		config.property(ClientProperties.FOLLOW_REDIRECTS, followRedirects);
		config.property(ClientProperties.READ_TIMEOUT, readTimeoutMs);
		config.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutMs);
		config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, asyncThreadPoolSize);

		return () -> ClientBuilder.newClient(config);
	}
}
