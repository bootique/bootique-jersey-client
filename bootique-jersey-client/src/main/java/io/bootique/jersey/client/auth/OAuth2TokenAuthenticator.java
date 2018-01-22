package io.bootique.jersey.client.auth;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

class OAuth2TokenAuthenticator implements ClientRequestFilter {

    private OAuth2TokenDAO tokenDAO;
    private volatile OAuth2Token lastToken;

    public OAuth2TokenAuthenticator(OAuth2Token initialToken, OAuth2TokenDAO tokenDAO) {
        this.tokenDAO = tokenDAO;
        this.lastToken = initialToken;
    }

    static String createAuthHeader(String token) {
        return "Bearer " + token;
    }

    public void filter(ClientRequestContext requestContext) {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", getAuthorization());
    }

    protected String getAuthorization() {

        if (lastToken.needsRefresh()) {
            synchronized (this) {
                if (lastToken.needsRefresh()) {
                    lastToken = tokenDAO.getToken();
                }
            }
        }

        return createAuthHeader(lastToken.getAccessToken());
    }
}
