package com.nhl.bootique.jersey.client;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.logback.LogbackModule;
import com.nhl.bootique.test.BQDaemonTestRuntime;

public class HttpClientFactoryFactory_LoggingIT {

	private BQDaemonTestRuntime serverApp;

	private void startApp(String config) {

		Consumer<Bootique> configurator = b -> {
			Module jersey = JerseyModule.builder().resource(Resource.class).build();
			b.modules(JettyModule.class, LogbackModule.class).module(jersey);
		};
		Function<BQDaemonTestRuntime, Boolean> startupCheck = r -> r.getRuntime().getInstance(Server.class).isStarted();

		serverApp = new BQDaemonTestRuntime(configurator, startupCheck, "--server",
				"--config=src/test/resources/com/nhl/bootique/jersey/client/" + config);
		serverApp.start(5, TimeUnit.SECONDS);
	}

	@After
	public void after() {
		if (serverApp != null) {
			serverApp.stop();
			serverApp = null;
		}
	}

	private Injector mockInjector;
	private File logsDir;

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
		assertEquals(lines.stream().collect(joining("\n")), 10, lines.size());
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
		assertTrue(Files.readAllLines(log.toPath()).isEmpty());
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
