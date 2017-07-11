package io.bootique.jersey.client;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.bootique.BQRuntime;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.client.auth.AuthenticatorFactory;
import io.bootique.jersey.client.auth.BasicAuthenticatorFactory;
import io.bootique.jetty.JettyModule;
import io.bootique.test.junit.BQDaemonTestFactory;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HttpClientFactoryFactoryIT {

    @ClassRule
    public static BQDaemonTestFactory SERVER_APP_FACTORY = new BQDaemonTestFactory();

    private static BQRuntime SERVER_APP;
    private Injector mockInjector;

    @BeforeClass
    public static void beforeClass() {
        Module jersey = (binder) -> JerseyModule.extend(binder).addResource(Resource.class);
        Function<BQRuntime, Boolean> startupCheck = r -> r.getInstance(Server.class).isStarted();

        SERVER_APP = SERVER_APP_FACTORY.app("--server")
                .modules(JettyModule.class, JerseyModule.class)
                .module(jersey)
                .startupCheck(startupCheck)
                .start();
    }

    @AfterClass
    public static void after() {
        SERVER_APP.shutdown();
    }

    @Before
    public void before() {
        mockInjector = mock(Injector.class);
    }

    @Test
    public void testCreateClientFactory_FollowRedirect() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setFollowRedirects(true);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("got", r.readEntity(String.class));
    }

    @Test
    public void testCreateClientFactory_NoFollowRedirect() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setFollowRedirects(false);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
        assertEquals(Status.TEMPORARY_REDIRECT.getStatusCode(), r.getStatus());
        assertEquals("http://127.0.0.1:8080/get", r.getHeaderString("location"));
    }

    @Test
    public void testCreateClientFactory_DefaultRedirect_NoFollow() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
        assertEquals(Status.TEMPORARY_REDIRECT.getStatusCode(), r.getStatus());
        assertEquals("http://127.0.0.1:8080/get", r.getHeaderString("location"));
    }

    @Test
    public void testCreateClientFactory_Compression() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setCompression(true);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/getbig").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("gzip", r.getHeaderString("Content-Encoding"));
    }

    @Test
    public void testCreateClientFactory_NoCompression() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setCompression(false);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/getbig").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertNull(r.getHeaderString("Content-Encoding"));
    }

    @Test
    public void testCreateClientFactory_CompressionDefault() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setCompression(true);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/getbig").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("gzip", r.getHeaderString("Content-Encoding"));
    }

    @Test
    public void testCreateClientFactory_NoTimeout() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("slowly_got", r.readEntity(String.class));
    }

    @Test
    public void testCreateClientFactory_LongTimeout() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setReadTimeoutMs(2000);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("slowly_got", r.readEntity(String.class));
    }

    @Test(expected = ProcessingException.class)
    public void testCreateClientFactory_ReadTimeout() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setReadTimeoutMs(50);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
    }

    @Test
    public void testCreateClientFactory_BasicAuth() {

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();

        BasicAuthenticatorFactory authenticator = new BasicAuthenticatorFactory();
        authenticator.setPassword("p1");
        authenticator.setUsername("u1");

        Map<String, AuthenticatorFactory> auth = new HashMap<>();
        auth.put("a1", authenticator);
        factoryFactory.setAuth(auth);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet())
                .newAuthenticatedClient("a1");

        Response r = client.target("http://127.0.0.1:8080/").path("/basicget").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("got_basic_Basic dTE6cDE=", r.readEntity(String.class));
    }

    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource {

        @GET
        @Path("get")
        public String get() {
            return "got";
        }

        @GET
        @Path("getbig")
        // value big enough to ensure compression kicks in
        public String getBig() {
            return "gotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgotgot";
        }

        @GET
        @Path("302")
        public Response threeOhTwo() throws URISyntaxException {
            return Response.temporaryRedirect(new URI("/get")).build();
        }

        @GET
        @Path("slowget")
        public String slowGet() throws InterruptedException {
            Thread.sleep(1000);
            return "slowly_got";
        }

        @GET
        @Path("basicget")
        public String basicGet(@HeaderParam("Authorization") String auth) {
            return "got_basic_" + auth;
        }
    }
}
