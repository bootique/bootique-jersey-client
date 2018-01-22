package io.bootique.jersey.client.auth;

import com.google.inject.Injector;
import io.bootique.jersey.JerseyModule;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class OAuth2AuthenticatorFactory_ExpirationIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory();

    @BeforeClass
    public static void beforeClass() {
        TEST_FACTORY
                .app("-s")
                .autoLoadModules()
                .module((binder) -> JerseyModule.extend(binder).addResource(TokenApi.class))
                .run();
    }

    @Test
    public void testGetWithToken() throws InterruptedException {

        OAuth2AuthenticatorFactory factory = new OAuth2AuthenticatorFactory();
        factory.setPassword("p");
        factory.setUsername("u");
        // the value must be bigger than token refresh lag of 3 sec. Otherwise we'd invariably get expired tokens...
        // so ... the test will be slower than ideal ... TODO: make the lag configurable
        factory.setExpiresIn(Duration.ofSeconds(3));
        factory.setTokenUrl("http://127.0.0.1:8080/token");

        ClientRequestFilter filter = factory.createAuthFilter(mock(Injector.class));

        WebTarget api = ClientBuilder
                .newClient()
                .register(filter)
                .target("http://127.0.0.1:8080/require_token");

        Response r1 = api.request().get();

        // send request immediately ... the old token should be valid
        Response r2 = api.request().get();

        // wait till the token expires, and send request
        Thread.sleep(1001);
        Response r3 = api.request().get();

        assertEquals(200, r1.getStatus());
        assertEquals("t:0", r1.readEntity(String.class));

        assertEquals(200, r2.getStatus());
        assertEquals("t:0", r2.readEntity(String.class));

        assertEquals(200, r3.getStatus());
        assertEquals("t:1", r3.readEntity(String.class));
    }

    @Path("/")
    public static class TokenApi {

        private static AtomicInteger COUNTER = new AtomicInteger(0);

        @POST
        @Path("token")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public String post(@FormParam("grant_type") String grantType, @HeaderParam("authorization") String auth) {
            return String.format("{\"access_token\":\"t:%s\",\"token_type\":\"example\"}", COUNTER.getAndIncrement());
        }

        @GET
        @Path("require_token")
        @Produces(MediaType.TEXT_PLAIN)
        public Response getWithToken(@HeaderParam("authorization") String auth) {
            return auth != null && auth.toLowerCase().startsWith("bearer ")
                    ? Response.ok().entity(auth.substring("bearer ".length())).build()
                    : Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
