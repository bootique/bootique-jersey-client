package com.nhl.bootique.jersey.client.instrumented;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

@Provider
public class ClientTimingFilter implements ClientRequestFilter, ClientResponseFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTimingFilter.class);
	private static final String TIMER_PROPERTY = ClientTimingFilter.class.getName() + ".timer";

	private Timer requestTimer;

	public ClientTimingFilter(MetricRegistry metricRegistry) {
		super();
		this.requestTimer = metricRegistry
				.timer(MetricRegistry.name(InstrumentedClientRequestFeature.class, "client-request-timer"));
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		Timer.Context requestTimerContext = requestTimer.time();
		requestContext.setProperty(TIMER_PROPERTY, requestTimerContext);

		LOGGER.info("client request started");

		// TODO: what is response "filter" below is not called? should we
		// collect and cancel timers manually to avoid a leak?
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		Timer.Context requestTimerContext = (Timer.Context) requestContext.getProperty(TIMER_PROPERTY);

		long timeNanos = requestTimerContext.stop();
		LOGGER.info("client request finished in {} ms", timeNanos / 1000000);
	}
}
