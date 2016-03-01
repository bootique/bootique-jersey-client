package com.nhl.bootique.jersey.client.auth;

import java.io.IOException;
import java.util.Objects;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAttribute;

import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.nhl.bootique.jersey.client.auth.BasicAuthenticatorFactory.BasicAuthenticator;

/**
 * @since 0.3
 */
@JsonTypeName("oauth2")
public class Oauth2AuthenticatorFactory implements AuthenticatorFactory {

	private String tokenUrl;
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

	public String getTokenUrl() {
		return tokenUrl;
	}

	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	@Override
	public ClientRequestFilter createAuthFilter(Configuration configuration) {

		return new TokenAuthenticator(getToken(configuration));
	}

	protected String getToken(Configuration configuration) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(tokenUrl);

		BasicAuthenticator tokenAuth = new BasicAuthenticator(username, password);

		Entity<String> postEntity = Entity.entity("grant_type=client_credentials", "application/x-www-form-urlencoded");
		Response tokenResponse = ClientBuilder.newClient(configuration).register(tokenAuth)
				.register(JacksonFeature.class).target(tokenUrl).request().post(postEntity);

		try {
			return readToken(tokenResponse);
		} finally {
			tokenResponse.close();
		}
	}

	private String readToken(Response response) {

		if (response.getStatus() != Status.OK.getStatusCode()) {
			String json = response.readEntity(String.class);
			String message = String.format("Error reading token: %s ... %s", response.getStatus(), json);
			throw new RuntimeException(message);
		}

		Token token = response.readEntity(Token.class);
		return Objects.requireNonNull(token).getAccessToken();
	}

	static class TokenAuthenticator implements ClientRequestFilter {

		private String authorization;

		public TokenAuthenticator(String token) {
			this.authorization = createTokenAuth(token);
		}

		public void filter(ClientRequestContext requestContext) throws IOException {
			MultivaluedMap<String, Object> headers = requestContext.getHeaders();
			headers.add("Authorization", authorization);
		}

		static String createTokenAuth(String token) {
			return "Bearer " + token;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Token {

		// TODO: expires, refresh token, etc...

		@XmlAttribute(name = "access_token")
		private String accessToken;

		public String getAccessToken() {
			return accessToken;
		}
	}
}
