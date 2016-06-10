package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.test.BQDaemonTestRuntime;
import com.nhl.bootique.test.BQTestRuntime;

public class ProviderInjectionIT {

	private static BQDaemonTestRuntime SERVER_APP;

	@BeforeClass
	public static void startJetty() {

		Consumer<Bootique> configurator = b -> {
			Module jersey = (binder) -> JerseyModule.contributeResources(binder).addBinding().to(Resource.class);

			b.module(JettyModule.class);
			b.module(JerseyModule.class);
			b.module(jersey);
		};

		SERVER_APP = new BQDaemonTestRuntime(configurator, r -> r.getRuntime().getInstance(Server.class).isStarted(),
				"--server");
		SERVER_APP.start(5, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void stopJetty() {
		SERVER_APP.stop();
	}

	private BQTestRuntime clientApp;

	@Before
	public void before() {

		Consumer<Bootique> clientConfigurator = b -> {
			b.module(JerseyClientModule.class);

			Module module = binder -> {
				JerseyClientModule.contributeFeatures(binder).addBinding().to(TestResponseReaderFeature.class);
				binder.bind(InjectedService.class);
			};
			b.module(module);
		};

		this.clientApp = new BQTestRuntime(clientConfigurator);
	}

	@Test
	public void testResponse() {

		Client client = clientApp.getRuntime().getInstance(HttpClientFactory.class).newClient();

		WebTarget target = client.target("http://127.0.0.1:8080/");

		Response r1 = target.request().get();
		assertEquals(Status.OK.getStatusCode(), r1.getStatus());
		assertEquals("[bare_string]_1", r1.readEntity(TestResponse.class).toString());
		r1.close();

		Response r2 = target.request().get();
		assertEquals(Status.OK.getStatusCode(), r2.getStatus());
		assertEquals("[bare_string]_2", r2.readEntity(TestResponse.class).toString());
		r2.close();
	}

	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public static class Resource {

		@GET
		public String get() {
			return "bare_string";
		}
	}

	public static class TestResponse {

		private String string;

		public TestResponse(String string) {
			this.string = string;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	public static class InjectedService {

		private AtomicInteger atomicInt = new AtomicInteger();

		public int getNext() {
			return atomicInt.incrementAndGet();
		}
	}

	public static class TestResponseReaderFeature implements Feature {
		@Override
		public boolean configure(FeatureContext context) {
			context.register(TestResponseReader.class);
			return true;
		}
	}

	@Provider
	public static class TestResponseReader implements MessageBodyReader<TestResponse> {

		private InjectedService service;

		@Inject
		public TestResponseReader(InjectedService service) {
			this.service = service;
		}

		@Override
		public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			return type.equals(TestResponse.class);
		}

		@Override
		public TestResponse readFrom(Class<TestResponse> type, Type genericType, Annotation[] annotations,
				MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
				throws IOException, WebApplicationException {

			String responseLine;
			try (BufferedReader in = new BufferedReader(new InputStreamReader(entityStream, "UTF-8"))) {
				responseLine = in.readLine();
			}

			String s = String.format("[%s]_%s", responseLine, service.getNext());
			return new TestResponse(s);
		}
	}

}
