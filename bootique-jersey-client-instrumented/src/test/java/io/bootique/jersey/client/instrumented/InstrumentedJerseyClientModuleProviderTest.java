package io.bootique.jersey.client.instrumented;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class InstrumentedJerseyClientModuleProviderTest {

	@Test
	public void testPresentInJar() {
		BQModuleProviderChecker.testPresentInJar(InstrumentedJerseyClientModuleProvider.class);
	}

}
