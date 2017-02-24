package io.bootique.jersey.client.instrumented;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @since 0.9
 */
public class InstrumentedFeature implements Feature {

    private ClientTimingFilter filter;

    public InstrumentedFeature(ClientTimingFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(filter);
        return true;
    }
}
