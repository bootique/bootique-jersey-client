package io.bootique.jersey.client.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.jersey.client.JerseyClientModule;

import javax.ws.rs.core.Feature;

public class InstrumentedJerseyClientModule extends ConfigModule {

	@Override
	public void configure(Binder binder) {
		JerseyClientModule.contributeFeatures(binder).addBinding()
				.to(Key.get(Feature.class, InstrumentedClientRequestFeature.class));
	}

	@Provides
	@Singleton
	@InstrumentedClientRequestFeature
	private Feature provideTimingFeature(ClientTimingFilter filter) {
		return c -> {
			c.register(filter);
			return true;
		};
	}

	@Provides
	@Singleton
	private ClientTimingFilter provideTimingFilter(MetricRegistry metricRegistry) {
		return new ClientTimingFilter(metricRegistry);
	}
}
