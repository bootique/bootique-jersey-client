package io.bootique.jersey.client.auth;

import com.google.inject.Injector;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.test.junit.JettyTestFactory;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class Oauth2AuthenticatorFactoryIT {

    @ClassRule
    public static JettyTestFactory SERVER_APP_FACTORY = new JettyTestFactory();

    @BeforeClass
    public static void beforeClass() {
        SERVER_APP_FACTORY
                .app()
                .autoLoadModules()
                .module((binder) -> JerseyModule.extend(binder).addResource(TokenApi.class))
                .start();
    }

    @Test
    public void testGetToken() {

        Oauth2AuthenticatorFactory factory = new Oauth2AuthenticatorFactory();
        factory.setPassword("p");
        factory.setUsername("u");
        factory.setTokenUrl("http://127.0.0.1:8080/token");

        assertEquals("t:client_credentials:Basic dTpw", factory.getToken());
    }

    @Test(expected = RuntimeException.class)
    public void testGetToken_Error() {

        Oauth2AuthenticatorFactory factory = new Oauth2AuthenticatorFactory();
        factory.setPassword("p");
        factory.setUsername("u");
        factory.setTokenUrl("http://127.0.0.1:8080/token_error");

        factory.getToken();
    }

    @Test
    public void testGetWithToken() {

        Oauth2AuthenticatorFactory factory = new Oauth2AuthenticatorFactory();
        factory.setPassword("p");
        factory.setUsername("u");
        factory.setTokenUrl("http://127.0.0.1:8080/token");

        ClientRequestFilter filter = factory.createAuthFilter(mock(Injector.class));

        Response r1 = ClientBuilder
                .newClient()
                .register(filter)
                .target("http://127.0.0.1:8080/require_token")
                .request()
                .get();

        assertEquals(200, r1.getStatus());

        Response r2 = ClientBuilder
                .newClient()
                .register(filter)
                .target("http://127.0.0.1:8080/require_token")
                .request()
                .get();

        assertEquals(200, r2.getStatus());
    }

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        @Path("token")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(@FormParam("grant_type") String grantType, @HeaderParam("authorization") String auth) {
            return String.format(
                    "{\"access_token\":\"t:%s:%s\",\"token_type\":\"example\","
                            + "\"expires_in\":3600,\"refresh_token\":\"bla\",\"example_parameter\":\"example_value\"}",
                    grantType, auth);
        }

        @POST
        @Path("token_error")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response post_error(@FormParam("grant_type") String grantType,
                                   @HeaderParam("authorization") String auth) {
            return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"invalid_request\"}").build();
        }

        @GET
        @Path("require_token")
        public Response getWithToken(@HeaderParam("authorization") String auth) {
            return auth != null && auth.toLowerCase().startsWith("bearer ")
                    ? Response.ok().build()
                    : Response.status(Status.BAD_REQUEST).build();
        }
    }
}
