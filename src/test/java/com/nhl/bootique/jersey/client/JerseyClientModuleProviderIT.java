package com.nhl.bootique.jersey.client;

import org.junit.Test;

import com.nhl.bootique.test.junit.BQModuleProviderChecker;

public class JerseyClientModuleProviderIT {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(JerseyClientModuleProvider.class);
	}
}
