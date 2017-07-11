package io.bootique.jersey.client;

import com.google.inject.Injector;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.logback.LogbackModule;
import io.bootique.test.junit.BQDaemonTestFactory;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class HttpClientFactoryFactory_LoggingIT {

    @Rule
    public BQDaemonTestFactory serverFactory = new BQDaemonTestFactory();
    private Injector mockInjector;
    private File logsDir;

    private void startApp(String config) {

        Module extensions = (binder) -> {
            JerseyModule.extend(binder).addResource(Resource.class);

            // TODO: this test is seriously dirty.. we don't start the client from Bootique,
            // yet we reuse Bootique Logback configuration for client logging.
            // so here we are turning off logging from the server....
            BQCoreModule.extend(binder)
                    .setLogLevel("org.eclipse.jetty.server", Level.OFF)
                    .setLogLevel("org.eclipse.jetty.util", Level.OFF);
        };

        Function<BQRuntime, Boolean> startupCheck = r -> r.getInstance(Server.class).isStarted();

        serverFactory.app("--server", "--config=src/test/resources/io/bootique/jersey/client/" + config)
                .modules(JettyModule.class, JerseyModule.class, LogbackModule.class)
                .module(extensions)
                .startupCheck(startupCheck)
                .start();
    }

    @Before
    public void before() {
        mockInjector = mock(Injector.class);
        logsDir = new File("target/logback");

        if (logsDir.exists()) {
            asList(logsDir.listFiles()).forEach(f -> f.delete());
        }
    }

    @Test
    public void testCreateClientFactory_Debug() throws IOException, InterruptedException {

        startApp("debug.yml");

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setFollowRedirects(true);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/get").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("got", r.readEntity(String.class));

        // wait for the log file to be flushed... there seems to be a race
        // condition in CI, resulting in assertions below not seeing the full
        // log
        Thread.sleep(500);

        File log = new File(logsDir, "debug.log");
        List<String> lines = Files.readAllLines(log.toPath());
        assertEquals(lines.stream().collect(joining("\n")), 11, lines.size());
        assertTrue(lines.get(0).contains("Sending client request on thread main"));
        assertTrue(lines.get(1).contains("GET http://127.0.0.1:8080/get"));
        assertTrue(lines.get(3).contains("Client response received on thread main"));
    }

    @Test
    public void testCreateClientFactory_Warn() throws IOException {

        startApp("warn.yml");

        HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
        factoryFactory.setFollowRedirects(true);
        Client client = factoryFactory.createClientFactory(mockInjector, Collections.emptySet()).newClient();

        Response r = client.target("http://127.0.0.1:8080/get").request().get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("got", r.readEntity(String.class));

        File log = new File(logsDir, "warn.log");
        List<String> lines = Files.readAllLines(log.toPath());
        assertEquals(Collections.emptyList(), lines);
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
