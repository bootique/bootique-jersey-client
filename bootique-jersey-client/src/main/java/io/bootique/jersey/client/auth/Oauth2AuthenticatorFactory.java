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
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * @since 0.3
 */
@JsonTypeName("oauth2")
@BQConfig("Authenticator for Oauth2 protocol. Includes URL of the OAuth token endpoint and " +
        "username/password that are exchanged for the token.")
public class Oauth2AuthenticatorFactory implements AuthenticatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2AuthenticatorFactory.class);

    protected String tokenUrl;
    protected String username;
    protected String password;

    public String getUsername() {
        return username;
    }

    @BQConfigProperty("Login username. A part of the application credentials to obtain oauth token.")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @BQConfigProperty("Password. A part of the application credentials to obtain oauth token.")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    @BQConfigProperty("A URL of the OAuth2 Token API endpoint.")
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    @Override
    public ClientRequestFilter createAuthFilter(Configuration clientConfig, Injector injector) {
        return new OAuth2TokenAuthenticator(() -> getToken(clientConfig));
    }

    protected String getToken(Configuration configuration) {
        
        Response tokenResponse = requestToken(configuration);

        try {
            String token = readToken(tokenResponse);
            LOGGER.info("Successfully obtained OAuth2 token");
            return token;
        } finally {
            tokenResponse.close();
        }
    }

    protected Response requestToken(Configuration configuration) {

        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(tokenUrl);

        LOGGER.info("reading OAuth2 token from " + tokenUrl);
        BasicAuthenticator tokenAuth = new BasicAuthenticator(username, password);

        Entity<String> postEntity = Entity.entity("grant_type=client_credentials",
                MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        return ClientBuilder.newClient(configuration).register(tokenAuth)
                .register(JacksonFeature.class).target(tokenUrl).request().post(postEntity);
    }

    protected String readToken(Response response) {

        if (response.getStatus() != Status.OK.getStatusCode()) {
            String json = response.readEntity(String.class);
            String message = String.format("Error reading token: %s ... %s", response.getStatus(), json);
            throw new RuntimeException(message);
        }

        Token token = response.readEntity(Token.class);
        return Objects.requireNonNull(token).getAccessToken();
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
