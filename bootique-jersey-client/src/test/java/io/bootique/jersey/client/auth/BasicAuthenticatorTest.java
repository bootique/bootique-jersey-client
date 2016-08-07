package io.bootique.jersey.client.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.bootique.jersey.client.auth.BasicAuthenticatorFactory.BasicAuthenticator;

public class BasicAuthenticatorTest {

	@Test
	public void testCreateBasicAuth() {
		assertEquals("Basic dTExMTpwMzQ1", BasicAuthenticator.createBasicAuth("u111", "p345"));
	}
}
