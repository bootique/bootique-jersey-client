package io.bootique.jersey.client.log;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class RequestLoggingFilter implements  ClientRequestFilter, ClientResponseFilter {

    private static final String REQUEST_PREFIX = "> ";
    private static final String PATTERN = "dd/MMM/yyyy:HH:mm:ss";

    private Logger logger;

    public RequestLoggingFilter() {
    }

    public RequestLoggingFilter(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {

        final StringBuilder sb = new StringBuilder();
        sb.append("Sending client request on thread ").append(Thread.currentThread().getName())
                .append("\n").append(REQUEST_PREFIX).append(requestContext.getMethod()).append(" ")
                .append(requestContext.getUri().toASCIIString()).append("\n");
        log(sb);
    }

    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {

        final StringBuilder logMessage = getResponseMessage(requestContext, responseContext);
        log(logMessage);
    }

    protected StringBuilder getResponseMessage(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {

        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern(PATTERN))).append("] ")
                .append("\"").append(requestContext.getMethod()).append(" ")
                .append(requestContext.getUri().getAuthority()).append(requestContext.getUri().getPath())
                .append("\" ").append(" Status: ").append(responseContext.getStatus());

        return sb;
    }

    private void log(final StringBuilder b) {
        if (logger != null) {
            logger.info(b.toString());
        }
    }
}