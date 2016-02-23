package com.nhl.bootique.jersey.client.unit;

import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;

public class ServerApp extends BaseTestApp {

	private Class<?> resource;

	public ServerApp(Class<?> resource) {
		this.resource = resource;
	}

	@Override
	protected void configure(Bootique bootique) {
		Module jersey = JerseyModule.builder().resource(resource).build();
		bootique.modules(JettyModule.class).module(jersey);
	}
}
