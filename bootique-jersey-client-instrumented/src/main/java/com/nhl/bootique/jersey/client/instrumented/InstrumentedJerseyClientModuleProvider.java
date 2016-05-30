package com.nhl.bootique.jersey.client.instrumented;

import java.util.Collection;
import java.util.Collections;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;
import com.nhl.bootique.jersey.client.JerseyClientModule;

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