package io.bootique.jersey.client.auth;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * @since 0.2
 */
@JsonTypeName("basic")
@BQConfig("Authenticator for BASIC auth protocol.")
public class BasicAuthenticatorFactory implements AuthenticatorFactory {

	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	@BQConfigProperty("A user name of the BASIC auth credentials used to access the remote resource.")
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	@BQConfigProperty("A password of the BASIC auth credentials used to access the remote resource.")
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public ClientRequestFilter createAuthFilter(Configuration clientConfig, Injector injector) {
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

		static String createBasicAuth(String username, String password) {
			String token = username + ":" + password;
			try {
				return "Basic " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new IllegalStateException("Cannot encode with UTF-8", ex);
			}
		}
	}
}
