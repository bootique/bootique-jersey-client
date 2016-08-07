package io.bootique.jersey.client;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;

import javax.ws.rs.core.Feature;
import java.util.Set;

public class JerseyClientModule extends ConfigModule {

	/**
	 * @param binder
	 *            DI binder passed to the Module that invokes this method.
	 * @since 0.3
	 * @return returns a {@link Multibinder} for client-side JAX-RS Features.
	 */
	public static Multibinder<Feature> contributeFeatures(Binder binder) {
		return Multibinder.newSetBinder(binder, Feature.class, JerseyClientFeatures.class);
	}

	@Override
	public void configure(Binder binder) {
		// trigger extension points creation and provide default contributions
		JerseyClientModule.contributeFeatures(binder);
	}

	@Provides
	@Singleton
	HttpClientFactory createClientFactory(ConfigurationFactory configurationFactory, Injector injector,
										  @JerseyClientFeatures Set<Feature> features) {
		return configurationFactory.config(HttpClientFactoryFactory.class, configPrefix).createClientFactory(injector,
				features);
	}
}
