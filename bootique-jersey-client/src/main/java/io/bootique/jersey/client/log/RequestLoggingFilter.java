package io.bootique.jersey.client.log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

public class RequestLoggingFilter implements  ClientRequestFilter, ClientResponseFilter {

    private static final String REQUEST_PREFIX = "> ";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("Sending client request.")
                .append("\n").append(REQUEST_PREFIX).append(requestContext.getMethod()).append(" ")
                .append(requestContext.getUri().toASCIIString()).append("\n");
        LOGGER.debug(sb.toString());
    }

    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
        final StringBuilder logMessage = getResponseMessage(requestContext, responseContext);
        LOGGER.info(logMessage.toString());
    }

    protected StringBuilder getResponseMessage(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
        final StringBuilder sb = new StringBuilder();
        sb.append(" Client response received.")
                .append(" \"").append(requestContext.getMethod()).append(" ")
                .append(requestContext.getUri().getAuthority()).append(requestContext.getUri().getPath())
                .append("\" ").append(" Status: ").append(responseContext.getStatus());
        return sb;
    }
}