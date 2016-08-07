package io.bootique.jersey.client;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class JerseyClientModuleProviderIT {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(JerseyClientModuleProvider.class);
	}
}
