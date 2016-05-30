package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.test.BQDaemonTestRuntime;
import com.nhl.bootique.test.BQTestRuntime;

public class CustomFeaturesIT {

	private static BQDaemonTestRuntime SERVER_APP;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {

		Consumer<Bootique> configurator = b -> {
			Module jersey = JerseyModule.builder().resource(Resource.class).build();
			b.modules(JettyModule.class).module(jersey);
		};
		Function<BQDaemonTestRuntime, Boolean> startupCheck = r -> r.getRuntime().getInstance(Server.class).isStarted();

		SERVER_APP = new BQDaemonTestRuntime(configurator, startupCheck, "--server");
		SERVER_APP.start(5, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void after() throws InterruptedException {
		SERVER_APP.stop();
	}

	private BQTestRuntime app;

	@Before
	public void before() {
		Consumer<Bootique> configurator = b -> {

			Module module = binder -> {
				JerseyClientModule.contributeFeatures(binder).addBinding().to(Feature1.class);
				JerseyClientModule.contributeFeatures(binder).addBinding().to(Feature2.class);
			};

			b.module(module).module(JerseyClientModule.class);
		};

		app = new BQTestRuntime(configurator);
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
