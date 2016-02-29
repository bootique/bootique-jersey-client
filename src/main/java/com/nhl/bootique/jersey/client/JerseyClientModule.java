package com.nhl.bootique.jersey.client;

import java.util.Set;

import javax.ws.rs.core.Feature;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;

public class JerseyClientModule extends ConfigModule {

	/**
	 * @param binder
	 *            DI binder passed to the Module that invokes this method.
	 * @since 0.3
	 * @return returns a {@link Multibinder} for client-side JAX-RS Features.
	 */
	public static Multibinder<Feature> contributeFeatures(Binder binder) {
		return Multibinder.newSetBinder(binder, Feature.class);
	}

	@Override
	public void configure(Binder binder) {
		// trigger extension points creation and provide default contributions
		JerseyClientModule.contributeFeatures(binder);
	}

	@Provides
	@Singleton
	HttpClientFactory createClientFactory(ConfigurationFactory configurationFactory, Set<Feature> features) {
		return configurationFactory.config(HttpClientFactoryFactory.class, configPrefix).createClientFactory(features);
	}
}
