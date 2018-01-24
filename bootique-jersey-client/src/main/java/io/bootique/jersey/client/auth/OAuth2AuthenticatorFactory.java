package io.bootique.jersey.client.auth;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.HttpClientFactory;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * @since 0.3
 */
@JsonTypeName("oauth2")
@BQConfig("Authenticator for Oauth2 protocol. Includes URL of the OAuth token endpoint and " +
        "username/password that are exchanged for the token.")
public class OAuth2AuthenticatorFactory implements AuthenticatorFactory {

    protected String tokenUrl;
    protected String username;
    protected String password;
    protected Duration expiresIn;

    public OAuth2AuthenticatorFactory() {
        this.expiresIn = Duration.ofHours(1);
    }

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

    @BQConfigProperty("A duration value for default token expiration. Will only be used for oauth servers that do " +
            "not send 'expires_in' attribute explicitly. If not set, this value is 1 hr.")
    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public ClientRequestFilter createAuthFilter(Injector injector) {
        OAuth2TokenDAO tokenDAO = createOAuth2TokenDAO(injector);
        return new OAuth2TokenAuthenticator(OAuth2Token.expiredToken(), tokenDAO);
    }

    protected OAuth2TokenDAO createOAuth2TokenDAO(Injector injector) {
        Objects.requireNonNull(username, "OAuth2 'username' is not specified");
        Objects.requireNonNull(password, "OAuth2 'password' is not specified");
        Objects.requireNonNull(tokenUrl, "OAuth2 'tokenUrl' is not specified");

        // defer initialization until HttpClientFactory becomes available.
        Function<String, WebTarget> tokenTargetFactory = tokenUrl -> injector
                .getInstance(HttpClientFactory.class)
                .newClient()
                .register(JacksonFeature.class)
                .target(tokenUrl);

        return new OAuth2TokenDAO(tokenTargetFactory, tokenUrl, username, password, expiresIn);
    }

}
