package io.bootique.jersey.client.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Module;
import io.bootique.BQRuntime;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.client.HttpClientFactory;
import io.bootique.jersey.client.JerseyClientModule;
import io.bootique.jetty.JettyModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.test.junit.BQDaemonTestFactory;
import io.bootique.test.junit.BQTestFactory;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InstrumentedClientIT {

    @ClassRule
    public static BQDaemonTestFactory SERVER_APP_FACTORY = new BQDaemonTestFactory();
    private static BQRuntime SERVER_APP;
    @Rule
    public BQTestFactory CLIENT_FACTORY = new BQTestFactory();
    private BQRuntime app;

    @BeforeClass
    public static void beforeClass() throws InterruptedException {

        Module jersey = binder -> JerseyModule.extend(binder).addResource(Resource.class);
        Function<BQRuntime, Boolean> startupCheck = r -> r.getInstance(Server.class).isStarted();

        SERVER_APP = SERVER_APP_FACTORY.app("--server")
                .modules(JettyModule.class, JerseyModule.class)
                .module(jersey)
                .startupCheck(startupCheck)
                .start();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        SERVER_APP.shutdown();
    }

    @Before
    public void before() {
        this.app = CLIENT_FACTORY
                .app()
                .modules(InstrumentedJerseyClientModule.class, JerseyClientModule.class, MetricsModule.class)
                .createRuntime();
    }

    @After
    public void after() {
        app.shutdown();
    }

    @Test
    public void testTimerInvoked() {

        HttpClientFactory factory = app.getInstance(HttpClientFactory.class);

        MetricRegistry metrics = app.getInstance(MetricRegistry.class);

        Collection<Timer> timers = metrics.getTimers().values();
        assertEquals(1, timers.size());
        Timer timer = timers.iterator().next();
        assertEquals(0, timer.getCount());

        factory.newClient().target("http://127.0.0.1:8080/get").request().get().close();
        assertEquals(1, timer.getCount());

        factory.newClient().target("http://127.0.0.1:8080/get").request().get().close();
        assertEquals(2, timer.getCount());
    }

    @Test
    public void testTimer_ConnectionError() {

        Client client = app.getInstance(HttpClientFactory.class).newClient();

        MetricRegistry metrics = app.getInstance(MetricRegistry.class);

        Collection<Timer> timers = metrics.getTimers().values();
        assertEquals(1, timers.size());
        Timer timer = timers.iterator().next();
        assertEquals(0, timer.getCount());

        // bad request: assuming nothing listens on port=8081
        try {
            client.target("http://127.0.0.1:8081/get").request().get().close();
            fail("Exception expected");
        } catch (ProcessingException e) {
            // ignore...
        }

        assertEquals(0, timer.getCount());

        // successful request
        client.target("http://127.0.0.1:8080/get").request().get().close();
        assertEquals(1, timer.getCount());
    }

    @Test
    public void testTimer_ServerErrors() {

        Client client = app.getInstance(HttpClientFactory.class).newClient();

        MetricRegistry metrics = app.getInstance(MetricRegistry.class);

        Collection<Timer> timers = metrics.getTimers().values();
        assertEquals(1, timers.size());
        Timer timer = timers.iterator().next();
        assertEquals(0, timer.getCount());

        client.target("http://127.0.0.1:8080/get500").request().get().close();
        assertEquals(1, timer.getCount());
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
        @Path("get500")
        public Response get500() {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
