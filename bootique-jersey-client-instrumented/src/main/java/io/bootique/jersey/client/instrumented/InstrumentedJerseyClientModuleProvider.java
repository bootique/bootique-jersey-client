package io.bootique.jersey.client.instrumented;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jersey.client.JerseyClientModule;

import java.util.Collection;
import java.util.Collections;

public class InstrumentedJerseyClientModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new InstrumentedJerseyClientModule();
	}

	@Override
	public Collection<Class<? extends Module>> overrides() {
		return Collections.singleton(JerseyClientModule.class);
	}
}