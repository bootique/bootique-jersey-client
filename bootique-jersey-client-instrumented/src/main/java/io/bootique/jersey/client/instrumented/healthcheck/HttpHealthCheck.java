package io.bootique.jersey.client.instrumented.healthcheck;

import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides a simple HTTP health check to verify that remote endpoints are alive and accessible.
 *
 * @since 0.25
 */
public class HttpHealthCheck implements HealthCheck {

    private Supplier<Response> responseSupplier;
    private Function<Response, HealthCheckOutcome> responseTester;

    protected HttpHealthCheck(
            Supplier<Response> responseSupplier,
            Function<Response, HealthCheckOutcome> responseTester) {
        this.responseSupplier = responseSupplier;
        this.responseTester = responseTester;
    }

    private static WebTarget followRedirects(WebTarget target) {
        return target.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE);
    }

    public static HttpHealthCheck viaHEAD(WebTarget target) {
        WebTarget localCopy = followRedirects(target);
        return new HttpHealthCheck(() -> localCopy.request().head(), HttpHealthCheck::testStatus);
    }

    public static HttpHealthCheck viaOPTIONS(WebTarget target) {
        WebTarget localCopy = followRedirects(target);
        return new HttpHealthCheck(() -> localCopy.request().options(), HttpHealthCheck::testStatus);
    }

    public static HttpHealthCheck viaGET(WebTarget target) {
        WebTarget localCopy = followRedirects(target);
        return new HttpHealthCheck(() -> localCopy.request().get(), HttpHealthCheck::testStatus);
    }

    protected static HealthCheckOutcome testStatus(Response response) {

        Response.StatusType status = response.getStatusInfo();

        // some fuzzy logic, mapping responses to outcomes
        // TODO: make it configurable..

        switch (status.getFamily()) {
            case SUCCESSFUL:
                return HealthCheckOutcome.ok();
            case REDIRECTION:
                // this is actually unexpected, as the client is configured to follow redirects...
                return HealthCheckOutcome.ok("Redirecting to " + response.getHeaderString("Location"));
            case CLIENT_ERROR:
                // 400 means health check is misconfigured.
                // 401 means the service is alive, just requires authorization.
                // and so on...
                return HealthCheckOutcome.warning("Health check is possibly misconfigured. Response status: "
                        + status.getStatusCode());
            default:
                return HealthCheckOutcome.critical("Remote API response status: " + status.getStatusCode());
        }
    }

    @Override
    public HealthCheckOutcome check() {
        // catch known errors. We can provide better HealthCheckOutcome than the generic exception handler downstream.

        Response response;

        try {
            response = responseSupplier.get();
        } catch (Exception e) {
            return onRequestException(e);
        }

        return responseTester.apply(response);
    }

    private HealthCheckOutcome onRequestException(Throwable e) {
        Throwable cause = e.getCause();

        if (cause instanceof ConnectException) {
            return HealthCheckOutcome.critical("Connection error: " + cause.getMessage());
        } else {
            return HealthCheckOutcome.critical(e);
        }
    }

}
