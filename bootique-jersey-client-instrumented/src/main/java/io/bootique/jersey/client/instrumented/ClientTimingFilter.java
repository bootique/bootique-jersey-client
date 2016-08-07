package io.bootique.jersey.client.instrumented;

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
		this.requestTimer = metricRegistry
				.timer(MetricRegistry.name(InstrumentedClientRequestFeature.class, "client-request-timer"));
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		Timer.Context requestTimerContext = requestTimer.time();
		requestContext.setProperty(TIMER_PROPERTY, requestTimerContext);

		LOGGER.info("Client request started");

		// note that response filter method may not be called at all if the
		// request results in connection exception, etc... Would be nice to
		// trace failed requests too, but nothing in JAX RS allows us to do
		// that directly...
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		Timer.Context requestTimerContext = (Timer.Context) requestContext.getProperty(TIMER_PROPERTY);

		// TODO: this timing does not take into account reading response
		// content... May need to add additional interceptor for that.
		long timeNanos = requestTimerContext.stop();

		LOGGER.info("Client request finished. Status: {}, time: {} ms.", responseContext.getStatus(),
				timeNanos / 1000000);
	}
}
