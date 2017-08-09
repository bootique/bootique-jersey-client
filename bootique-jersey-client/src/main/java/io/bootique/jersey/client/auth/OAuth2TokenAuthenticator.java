package io.bootique.jersey.client.auth;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.function.Supplier;

class OAuth2TokenAuthenticator implements ClientRequestFilter {

    private Supplier<String> tokenSupplier;
    private volatile String authorization;

    static String createTokenAuth(String token) {
        return "Bearer " + token;
    }

    public OAuth2TokenAuthenticator(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", getAuthorization());
    }

    protected String getAuthorization() {

        if (authorization == null) {

            synchronized (this) {
                if (authorization == null) {
                    authorization = createTokenAuth(tokenSupplier.get());
                }
            }
        }

        return authorization;
    }
}
