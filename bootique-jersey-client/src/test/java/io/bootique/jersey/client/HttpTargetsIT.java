package io.bootique.jersey.client;

import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.logback.LogbackModuleProvider;
import io.bootique.test.junit.BQTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class HttpTargetsIT {


    @ClassRule
    public static BQTestFactory SERVER_FACTORY = new BQTestFactory();

    @Rule
    public BQTestFactory clientFactory = new BQTestFactory();

    @BeforeClass
    public static void beforeClass() {
        SERVER_FACTORY.app("--server")
                .modules(JettyModule.class, JerseyModule.class)
                .module(new LogbackModuleProvider())
                .module(b -> JerseyModule.extend(b).addResource(Resource.class))
                .run();
    }

    @Test
    public void testNewTarget() {
        HttpTargets factory =
                clientFactory.app()
                        .module(new JerseyClientModuleProvider())
                        .module(new LogbackModuleProvider())
                        .property("bq.jerseyclient.targets.t1.url", "http://127.0.0.1:8080/get")
                        .createRuntime()
                        .getInstance(HttpTargets.class);

        WebTarget t1 = factory.newTarget("t1");

        Response r1 = t1.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("got", r1.readEntity(String.class));

        Response r2 = t1.path("me").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("got/me", r2.readEntity(String.class));
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
        @Path("get/me")
        public String getMe() {
            return "got/me";
        }

        @GET
        @Path("get_auth")
        public String getAuth(@HeaderParam("Authorization") String auth) {
            return "got_" + auth;
        }
    }
}
