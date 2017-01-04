package io.bootique.jersey.client.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.auth.BasicAuthenticatorFactory.BasicAuthenticator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.IOException;
import java.util.Objects;

/**
 * @since 0.3
 */
@JsonTypeName("oauth2")
@BQConfig
public class Oauth2AuthenticatorFactory implements AuthenticatorFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2AuthenticatorFactory.class);

	private String tokenUrl;
	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	@BQConfigProperty
	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	@BQConfigProperty
	public void setPassword(String password) {
		this.password = password;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	@BQConfigProperty
	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	@Override
	public ClientRequestFilter createAuthFilter(Configuration clientConfig, Injector injector) {
		return new TokenAuthenticator(getToken(clientConfig));
	}

	protected String getToken(Configuration configuration) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(tokenUrl);

		LOGGER.info("reading OAuth2 token from " + tokenUrl);

		BasicAuthenticator tokenAuth = new BasicAuthenticator(username, password);

		Entity<String> postEntity = Entity.entity("grant_type=client_credentials",
				MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		Response tokenResponse = ClientBuilder.newClient(configuration).register(tokenAuth)
				.register(JacksonFeature.class).target(tokenUrl).request().post(postEntity);

		try {
			String token = readToken(tokenResponse);
			LOGGER.info("Successfully obtained OAuth2 token");
			return token;
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
