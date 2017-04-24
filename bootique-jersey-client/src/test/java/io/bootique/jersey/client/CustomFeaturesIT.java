package io.bootique.jersey.client;

import com.google.inject.Module;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.test.BQDaemonTestRuntime;
import io.bootique.test.BQTestRuntime;
import io.bootique.test.junit.BQDaemonTestFactory;
import io.bootique.test.junit.BQTestFactory;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomFeaturesIT {

    @ClassRule
    public static BQDaemonTestFactory SERVER_APP_FACTORY = new BQDaemonTestFactory();
    private static BQDaemonTestRuntime SERVER_APP;
    @Rule
    public BQTestFactory CLIENT_FACTORY = new BQTestFactory();
    private BQTestRuntime app;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {

        Module jersey = (binder) -> JerseyModule.extend(binder).addResource(Resource.class);
        Function<BQDaemonTestRuntime, Boolean> startupCheck = r -> r.getRuntime().getInstance(Server.class).isStarted();

        SERVER_APP = SERVER_APP_FACTORY.app("--server")
                .modules(JettyModule.class, JerseyModule.class)
                .module(jersey)
                .startupCheck(startupCheck)
                .start();
    }

    @AfterClass
    public static void after() throws InterruptedException {
        SERVER_APP.stop();
    }

    @Before
    public void before() {
        Module module = binder -> JerseyClientModule.extend(binder).addFeature(Feature1.class).addFeature(Feature2.class);
        this.app = CLIENT_FACTORY.app().module(JerseyClientModule.class).module(module).createRuntime();
    }

    @Test
    public void testFeaturesLoaded() {

        assertFalse(Feature1.LOADED);
        assertFalse(Feature2.LOADED);

        HttpClientFactory factory = app.getRuntime().getInstance(HttpClientFactory.class);
        factory.newClient().target("http://127.0.0.1:8080/").request().get().close();

        assertTrue(Feature1.LOADED);
        assertTrue(Feature2.LOADED);
    }

    static class Feature1 implements Feature {

        static boolean LOADED = false;

        @Override
        public boolean configure(FeatureContext c) {
            LOADED = true;
            return true;
        }
    }

    static class Feature2 implements Feature {

        static boolean LOADED = false;

        @Override
        public boolean configure(FeatureContext c) {
            LOADED = true;
            return true;
        }
    }

    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class Resource {

        @GET
        @Path("get")
        public String get() {
            return "got";
        }
    }
}
