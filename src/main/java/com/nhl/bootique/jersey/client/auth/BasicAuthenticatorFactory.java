package com.nhl.bootique.jersey.client.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;

/**
 * @since 0.2
 */
public class BasicAuthenticatorFactory implements AuthenticatorFactory {

	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public ClientRequestFilter createAuthFilter() {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		return new BasicAuthenticator(username, password);
	}

	static class BasicAuthenticator implements ClientRequestFilter {

		private String basicAuth;

		public BasicAuthenticator(String username, String password) {
			this.basicAuth = createBasicAuth(username, password);
		}

		public void filter(ClientRequestContext requestContext) throws IOException {
			MultivaluedMap<String, Object> headers = requestContext.getHeaders();
			headers.add("Authorization", basicAuth);
		}

		private String createBasicAuth(String username, String password) {
			String token = username + ":" + password;
			try {
				return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Cannot encode with UTF-8", ex);
			}
		}
	}
}
