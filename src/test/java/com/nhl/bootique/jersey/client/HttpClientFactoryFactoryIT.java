package com.nhl.bootique.jersey.client;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
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

import com.nhl.bootique.jersey.client.unit.ServerApp;

public class HttpClientFactoryFactoryIT {

	private static ExecutorService executor;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			return new ServerApp(Resource.class).run("--server");
		});
		// wait for Jetty to start ...
		Thread.sleep(2000);
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
	}
}
