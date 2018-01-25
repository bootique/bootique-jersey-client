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
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class HttpTargets_FeaturesIT {


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
    public void testFeatures() {
        HttpTargets targets =
                clientFactory.app()
                        .module(new JerseyClientModuleProvider())
                        .module(new LogbackModuleProvider())
                        .property("bq.jerseyclient.targets.t.url", "http://127.0.0.1:8080/get")
                        .createRuntime()
                        .getInstance(HttpTargets.class);

        // create two copies of the same endpoint, but with different feature sets...
        WebTarget t1 = targets.newTarget("t").register(Feature1.class);
        WebTarget t2 = targets.newTarget("t").register(new Feature2());

        Response r1 = t1.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("got_f1:true_f2:null", r1.readEntity(String.class));

        Response r2 = t2.request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r2.getStatus());
        assertEquals("got_f1:null_f2:true", r2.readEntity(String.class));
    }

    static class Feature1 implements Feature {

        @Override
        public boolean configure(FeatureContext c) {
            c.register((ClientRequestFilter) requestContext -> {
                requestContext.getHeaders().add("f1", "true");
            });
            return true;
        }
    }

    static class Feature2 implements Feature {

        @Override
        public boolean configure(FeatureContext c) {

            c.register((ClientRequestFilter) requestContext -> {
                requestContext.getHeaders().add("f2", "true");
            });

            return true;
        }
    }

    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource {

        @Context
        Configuration configuration;

        @GET
        @Path("get")
        public String get(@HeaderParam("f1") String f1, @HeaderParam("f2") String f2) {
            return String.format("got_f1:%s_f2:%s", f1, f2);
        }
    }
}
