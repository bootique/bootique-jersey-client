package com.nhl.bootique.jersey.client;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.nhl.bootique.ConfigModule;
import com.nhl.bootique.config.ConfigurationFactory;

public class JerseyClientModule extends ConfigModule {

	@Provides
	@Singleton
	HttpClientFactory createClientFactory(ConfigurationFactory configurationFactory) {
		return configurationFactory.config(HttpClientFactoryFactory.class, configPrefix).createClientFactory();
	}
}
