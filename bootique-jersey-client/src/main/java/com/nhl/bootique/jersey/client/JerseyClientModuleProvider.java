package com.nhl.bootique.jersey.client;

import com.google.inject.Module;
import com.nhl.bootique.BQModuleProvider;

public class JerseyClientModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new JerseyClientModule();
	}
}
