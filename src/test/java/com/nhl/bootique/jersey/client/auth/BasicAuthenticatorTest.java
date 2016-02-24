package com.nhl.bootique.jersey.client.auth;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.nhl.bootique.jersey.client.auth.BasicAuthenticatorFactory.BasicAuthenticator;

public class BasicAuthenticatorTest {

	@Test
	public void testCreateBasicAuth() {
		assertEquals("BASIC dTExMTpwMzQ1", BasicAuthenticator.createBasicAuth("u111", "p345"));
	}
}
