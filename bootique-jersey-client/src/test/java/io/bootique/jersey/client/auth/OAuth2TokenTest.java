package io.bootique.jersey.client.auth;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OAuth2TokenTest {

    @Test
    public void testExpiredToken() {
        OAuth2Token token = OAuth2Token.expiredToken();
        assertNotNull(token);
        assertTrue(token.needsRefresh());
    }

    @Test
    public void testToken() {
        OAuth2Token token = OAuth2Token.token("abcd", LocalDateTime.now().plusDays(1));
        assertNotNull(token);
        assertFalse(token.needsRefresh());
        assertEquals("abcd", token.getAccessToken());
    }
}
