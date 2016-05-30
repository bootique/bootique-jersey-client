package com.nhl.bootique.jersey.client.instrumented;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jersey.client.HttpClientFactory;
import com.nhl.bootique.jersey.client.JerseyClientModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.logback.LogbackModule;
import com.nhl.bootique.metrics.MetricsModule;
import com.nhl.bootique.test.BQDaemonTestRuntime;
import com.nhl.bootique.test.BQTestRuntime;

public class InstrumentedClientIT {

	private static BQDaemonTestRuntime SERVER_APP;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {

		Consumer<Bootique> configurator = b -> {
			Module jersey = binder -> JerseyModule.contributeResources(binder).addBinding().to(Resource.class);
			b.modules(JettyModule.class, LogbackModule.class, JerseyModule.class).module(jersey);
		};
		Function<BQDaemonTestRuntime, Boolean> startupCheck = r -> r.getRuntime().getInstance(Server.class).isStarted();

		SERVER_APP = new BQDaemonTestRuntime(configurator, startupCheck, "--server");
		SERVER_APP.start(5, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void afterClass() throws InterruptedException {
		SERVER_APP.stop();
	}

	private BQTestRuntime app;

	@Before
	public void before() {
		Consumer<Bootique> configurator = b -> {
			b.modules(InstrumentedJerseyClientModule.class, JerseyClientModule.class, LogbackModule.class,
					MetricsModule.class);
		};

		app = new BQTestRuntime(configurator);
	}

	@After
	public void after() {
		app.stop();
	}

	@Test
	public void testTimerInvoked() {

		HttpClientFactory factory = app.getRuntime().getInstance(HttpClientFactory.class);

		MetricRegistry metrics = app.getRuntime().getInstance(MetricRegistry.class);

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

		Client client = app.getRuntime().getInstance(HttpClientFactory.class).newClient();

		MetricRegistry metrics = app.getRuntime().getInstance(MetricRegistry.class);

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

		Client client = app.getRuntime().getInstance(HttpClientFactory.class).newClient();

		MetricRegistry metrics = app.getRuntime().getInstance(MetricRegistry.class);

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
