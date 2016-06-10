package com.nhl.bootique.jersey.client.auth;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Module;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.jersey.JerseyModule;
import com.nhl.bootique.jetty.JettyModule;
import com.nhl.bootique.test.BQDaemonTestRuntime;

public class Oauth2AuthenticatorFactoryIT {

	private static BQDaemonTestRuntime SERVER_APP;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {

		Consumer<Bootique> configurator = b -> {
			Module jersey = (binder) -> JerseyModule.contributeResources(binder).addBinding().to(TokenApi.class);
			b.modules(JettyModule.class, JerseyModule.class).module(jersey);
		};
		Function<BQDaemonTestRuntime, Boolean> startupCheck = r -> r.getRuntime().getInstance(Server.class).isStarted();

		SERVER_APP = new BQDaemonTestRuntime(configurator, startupCheck, "--server");
		SERVER_APP.start(5, TimeUnit.SECONDS);
	}

	@AfterClass
	public static void after() throws InterruptedException {
		SERVER_APP.stop();
	}

	@Test
	public void testGetToken() {

		Oauth2AuthenticatorFactory factory = new Oauth2AuthenticatorFactory();
		factory.setPassword("p");
		factory.setUsername("u");
		factory.setTokenUrl("http://127.0.0.1:8080/token");

		ClientConfig config = new ClientConfig();
		assertEquals("t:client_credentials:Basic dTpw", factory.getToken(config));
	}

	@Test(expected = RuntimeException.class)
	public void testGetToken_Error() {

		Oauth2AuthenticatorFactory factory = new Oauth2AuthenticatorFactory();
		factory.setPassword("p");
		factory.setUsername("u");
		factory.setTokenUrl("http://127.0.0.1:8080/token_error");

		ClientConfig config = new ClientConfig();
		factory.getToken(config);
	}

	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public static class TokenApi {

		@POST
		@Path("token")
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		public String post(@FormParam("grant_type") String grantType, @HeaderParam("authorization") String auth) {
			return String.format(
					"{\"access_token\":\"t:%s:%s\",\"token_type\":\"example\","
							+ "\"expires_in\":3600,\"refresh_token\":\"bla\",\"example_parameter\":\"example_value\"}",
					grantType, auth);
		}

		@POST
		@Path("token_error")
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		public Response post_error(@FormParam("grant_type") String grantType,
				@HeaderParam("authorization") String auth) {
			return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"invalid_request\"}").build();
		}
	}
}
