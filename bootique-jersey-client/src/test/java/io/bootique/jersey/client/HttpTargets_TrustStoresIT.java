package io.bootique.jersey.client;

import com.google.inject.ProvisionException;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.logback.LogbackModuleProvider;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class HttpTargets_TrustStoresIT {

    // hostname must be 'localhost'... '127.0.0.1' will cause SSL errors
    private static final String SERVICE_URL = "https://localhost:14001/get";
    private static final String CLIENT_TRUST_STORE = "classpath:io/bootique/jersey/client/testkeystore_default_password";

    @ClassRule
    public static BQTestFactory SERVER_FACTORY = new BQTestFactory();

    @Rule
    public BQTestFactory clientFactory = new BQTestFactory();

    @BeforeClass
    public static void beforeClass() {
        SERVER_FACTORY.app("-s", "-c", "classpath:io/bootique/jersey/client/TrustStoresIT_server.yml")
                .modules(JettyModule.class, JerseyModule.class)
                .module(new LogbackModuleProvider())
                .module(b -> JerseyModule.extend(b).addResource(Resource.class))
                .run();
    }

    @Test
    public void testNamedTrustStore() {

        HttpTargets targets =
                clientFactory.app()
                        .module(new JerseyClientModuleProvider())
                        .module(new LogbackModuleProvider())
                        .property("bq.jerseyclient.trustStores.ts1.location", CLIENT_TRUST_STORE)
                        .property("bq.jerseyclient.targets.t.url", SERVICE_URL)
                        .property("bq.jerseyclient.targets.t.trustStore", "ts1")
                        .createRuntime()
                        .getInstance(HttpTargets.class);

        Response r = targets.newTarget("t").request().get();
        Resource.assertResponse(r);
    }

    @Test(expected = ProvisionException.class)
    public void testNamedTrustStore_InvalidRef() {

        clientFactory.app()
                .module(new JerseyClientModuleProvider())
                .module(new LogbackModuleProvider())
                .property("bq.jerseyclient.targets.t.url", SERVICE_URL)
                .property("bq.jerseyclient.targets.t.trustStore", "ts1")
                .createRuntime()
                .getInstance(HttpTargets.class);
    }

    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource {

        static void assertResponse(Response response) {
            assertEquals(200, response.getStatus());
            assertEquals("got", response.readEntity(String.class));
        }

        @GET
        @Path("get")
        public String get() {
            return "got";
        }
    }
}
