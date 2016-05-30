package com.nhl.bootique.jersey.client.instrumented;

import org.junit.Test;

import com.nhl.bootique.test.junit.BQModuleProviderChecker;

public class InstrumentedJerseyClientModuleProviderTest {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(InstrumentedJerseyClientModuleProvider.class);
	}

}
