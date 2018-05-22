package io.bootique.jersey.client.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.bootique.jersey.client.log.RequestLoggingFilter;
import io.bootique.metrics.MetricNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.ext.Provider;

@Provider
public class ClientTimingFilter extends RequestLoggingFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTimingFilter.class);
	private static final String TIMER_PROPERTY = ClientTimingFilter.class.getName() + ".timer";

	private Timer requestTimer;

	public ClientTimingFilter(MetricRegistry metricRegistry) {
		String name = MetricNaming.forModule(JerseyClientInstrumentedModule.class).name("Client", "RequestTimer");
		this.requestTimer = metricRegistry.timer(name);
	}

	@Override
	public void filter(ClientRequestContext requestContext) {
		Timer.Context requestTimerContext = requestTimer.time();
		requestContext.setProperty(TIMER_PROPERTY, requestTimerContext);

		LOGGER.debug("Client request started");

		// note that response filter method may not be called at all if the
		// request results in connection exception, etc... Would be nice to
		// trace failed requests too, but nothing in JAX RS allows us to do
		// that directly...
	}

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
		StringBuilder logMessage = getResponseMessage(requestContext, responseContext);
		Timer.Context requestTimerContext = (Timer.Context) requestContext.getProperty(TIMER_PROPERTY);

		// TODO: this timing does not take into account reading response
		// content... May need to add additional interceptor for that.
		long timeNanos = requestTimerContext.stop();

		logMessage.append(" time: ").append(timeNanos / 1000000).append(" ms.");
		LOGGER.debug(logMessage.toString());
	}
}
