package io.bootique.jersey.client.auth;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a token received from a remote oauth server.
 *
 * @since 0.25
 */
public class OAuth2Token {

    private String accessToken;
    private LocalDateTime refreshAfter;

    protected OAuth2Token(String accessToken, LocalDateTime refreshAfter) {
        this.accessToken = accessToken;
        this.refreshAfter = refreshAfter;
    }

    /**
     * A factory method for an expired token that can be used as a placeholder initial token.
     *
     * @return a token that can be used as an initial placeholder for authenticator.
     */
    public static OAuth2Token expiredToken() {
        return new OAuth2Token("*placeholder_token*", LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    }

    public static OAuth2Token token(String accessToken, LocalDateTime expiresOn) {
        Objects.requireNonNull(accessToken, "'accessToken' is null");
        Objects.requireNonNull(expiresOn, "'expiresOn' is null");

        // refresh the token if it is still fresh, but is about to expire... The hope is this improves reliability.
        // Though in fact we have no idea... E.g. an attempt to refresh a token before it is expired may result in
        // the same token returned from the server (?) so this may be a dubious optimization...
        // TODO: Need to test with common oauth servers (Google, FB, GitHub), and maybe make configurable as "refreshDrift"  or something.
        LocalDateTime refreshAfter = Objects.requireNonNull(expiresOn).minusSeconds(2);
        return new OAuth2Token(accessToken, refreshAfter);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean needsRefresh() {
        return refreshAfter.isBefore(LocalDateTime.now());
    }

    public LocalDateTime getRefreshAfter() {
        return refreshAfter;
    }
}
