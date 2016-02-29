package com.nhl.bootique.jersey.client;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;

import com.google.inject.Injector;

public class ClientGuiceBridgeFeature implements Feature {

	private static final String INJECTOR_PROPERTY = "com.nhl.bootique.jersey.client.injector";

	// TODO: can all of this happen inside this Feature "configure(..)" method?
	static void register(ClientConfig config, Injector injector) {
		config.property(ClientGuiceBridgeFeature.INJECTOR_PROPERTY, injector);
		config.register(ClientGuiceBridgeFeature.class);
	}

	static Injector getInjector(Configuration configuration) {
		Injector injector = (Injector) configuration.getProperty(ClientGuiceBridgeFeature.INJECTOR_PROPERTY);
		if (injector == null) {
			throw new IllegalStateException("Injector is not available in JAX RS runtime. Use property '"
					+ ClientGuiceBridgeFeature.INJECTOR_PROPERTY + "' to set it");
		}

		return injector;
	}

	@Override
	public boolean configure(FeatureContext context) {

		context.register(new AbstractBinder() {

			@Override
			protected void configure() {

				Injector injector = ClientGuiceBridgeFeature.getInjector(context.getConfiguration());
				ClientGuiceInjectInjector injectInjector = new ClientGuiceInjectInjector(injector);

				bind(injectInjector).to(new TypeLiteral<InjectionResolver<com.google.inject.Inject>>() {
				});
			}
		});

		return true;
	}
}
