package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.bootique.jersey.client.auth.AuthenticatorFactory;
import com.nhl.bootique.jersey.client.auth.BasicAuthenticatorFactory;
import com.nhl.bootique.jersey.client.unit.ServerApp;

public class HttpClientFactoryFactoryIT {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactoryFactoryIT.class);

	private static ExecutorService executor;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			return new ServerApp(Resource.class).run("--server");
		});

		// check for Jetty to start
		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		Client client = factoryFactory.createClientFactory().newClient();
		ProcessingException lastException = null;

		for (int i = 0; i < 10; i++) {

			Thread.sleep(500);

			try {
				client.target("http://127.0.0.1:8080/").request().get().close();
				lastException = null;
				break;
			} catch (ProcessingException e) {
				lastException = e;
				LOGGER.info("Jetty is not available yet...");
			}
		}

		if (lastException != null) {
			LOGGER.info("Can't start Jetty. Last connect attempt ended in error", lastException);
			fail("Can't start Jetty");
		} else {
			LOGGER.info("Jetty started... will start running tests");
		}
	}

	@AfterClass
	public static void after() throws InterruptedException {
		executor.shutdownNow();
		executor.awaitTermination(3, TimeUnit.SECONDS);
	}

	@Test
	public void testCreateClientFactory_FollowRedirect() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setFollowRedirects(true);
		Client client = factoryFactory.createClientFactory().newClient();

		Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		assertEquals("got", r.readEntity(String.class));
	}

	@Test
	public void testCreateClientFactory_NoFollowRedirect() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setFollowRedirects(false);
		Client client = factoryFactory.createClientFactory().newClient();

		Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
		assertEquals(Status.TEMPORARY_REDIRECT.getStatusCode(), r.getStatus());
		assertEquals("http://127.0.0.1:8080/get", r.getHeaderString("location"));
	}

	@Test
	public void testCreateClientFactory_DefaultRedirect_NoFollow() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		Client client = factoryFactory.createClientFactory().newClient();

		Response r = client.target("http://127.0.0.1:8080/").path("/302").request().get();
		assertEquals(Status.TEMPORARY_REDIRECT.getStatusCode(), r.getStatus());
		assertEquals("http://127.0.0.1:8080/get", r.getHeaderString("location"));
	}

	@Test
	public void testCreateClientFactory_NoTimeout() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		Client client = factoryFactory.createClientFactory().newClient();

		Response r = client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		assertEquals("slowly_got", r.readEntity(String.class));
	}

	@Test
	public void testCreateClientFactory_LongTimeout() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setReadTimeoutMs(2000);
		Client client = factoryFactory.createClientFactory().newClient();

		Response r = client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		assertEquals("slowly_got", r.readEntity(String.class));
	}

	@Test(expected = ProcessingException.class)
	public void testCreateClientFactory_ReadTimeout() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();
		factoryFactory.setReadTimeoutMs(50);
		Client client = factoryFactory.createClientFactory().newClient();

		client.target("http://127.0.0.1:8080/").path("/slowget").request().get();
	}

	@Test
	public void testCreateClientFactory_BasicAuth() {

		HttpClientFactoryFactory factoryFactory = new HttpClientFactoryFactory();

		BasicAuthenticatorFactory authenticator = new BasicAuthenticatorFactory();
		authenticator.setPassword("p1");
		authenticator.setUsername("u1");

		Map<String, AuthenticatorFactory> auth = new HashMap<>();
		auth.put("a1", authenticator);
		factoryFactory.setAuth(auth);
		Client client = factoryFactory.createClientFactory().newAuthenticatedClient("a1");

		Response r = client.target("http://127.0.0.1:8080/").path("/basicget").request().get();
		assertEquals(Status.OK.getStatusCode(), r.getStatus());
		assertEquals("got_basic_BASIC dTE6cDE=", r.readEntity(String.class));
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
		@Path("302")
		public Response threeOhTwo() throws URISyntaxException {
			return Response.temporaryRedirect(new URI("/get")).build();
		}

		@GET
		@Path("slowget")
		public String slowGet() throws InterruptedException {
			Thread.sleep(1000);
			return "slowly_got";
		}

		@GET
		@Path("basicget")
		public String basicGet(@HeaderParam("Authorization") String auth) {
			return "got_basic_" + auth;
		}
	}
}
