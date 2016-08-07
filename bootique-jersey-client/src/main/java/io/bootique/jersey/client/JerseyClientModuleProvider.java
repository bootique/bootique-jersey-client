package io.bootique.jersey.client;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class JerseyClientModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new JerseyClientModule();
	}
}
