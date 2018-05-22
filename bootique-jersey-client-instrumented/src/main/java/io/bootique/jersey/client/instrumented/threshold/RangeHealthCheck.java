package io.bootique.jersey.client.instrumented.threshold;

import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.HealthCheckOutcome;


/**
 * Provides a value range health check to measure response time according to thresholds.
 *
 * @since 0.26
 */
public class RangeHealthCheck implements HealthCheck {

    private HealthCheck healthCheck;

    protected RangeHealthCheck (
            HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @Override
    public HealthCheckOutcome check() throws Exception {
        return healthCheck.check();
    }

}
