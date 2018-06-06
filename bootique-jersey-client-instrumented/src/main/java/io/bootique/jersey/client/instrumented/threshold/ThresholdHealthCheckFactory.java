package io.bootique.jersey.client.instrumented.threshold;

import com.codahale.metrics.MetricRegistry;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.instrumented.ClientTimingFilter;
import io.bootique.jersey.client.instrumented.JerseyClientInstrumentedModule;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.DoubleRangeFactory;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.metrics.health.check.ValueRangeCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @since 0.26
 */
@BQConfig
public class ThresholdHealthCheckFactory {

    public static final String THRESHOLD_REQUESTS_CHECK =  JerseyClientInstrumentedModule
            .METRIC_NAMING
            .name("Threshold", "Requests");


    private DoubleRangeFactory timeRequestsThresholds;
    private MetricRegistry metricRegistry;

    public ThresholdHealthCheckFactory initMetricRegistry() {
        return this;
    }

    @BQConfigProperty
    public void setTimeRequestsThresholds(DoubleRangeFactory timeRequestsThresholds) {
        this.timeRequestsThresholds = timeRequestsThresholds;
    }

    public JerseyHealthChecks createThresholdHealthCheck(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;

        return new JerseyHealthChecks(createHealthChecksMap(getMetricRegistry()));
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    private HealthCheck createTimeRequestsCheck(MetricRegistry registry) {
        Supplier<Double> deferredGauge = valueFromGauge(registry, ClientTimingFilter.TIMER_NAME);

        ValueRange<Double> range = getTimeRequestsThresholds();
        return new ValueRangeCheck<>(range, deferredGauge);
    }

    protected ValueRange<Double> getTimeRequestsThresholds() {

        // init min if it wasn't set...
        if (timeRequestsThresholds != null) {
            if (timeRequestsThresholds.getMin() == null) {
                timeRequestsThresholds.setMin(0);
            }

            return timeRequestsThresholds.createRange();
        }

        return ValueRange.builder(Double.class).min(0.0).warning(3.0).critical(15.0).build();
    }

    protected Map<String, HealthCheck> createHealthChecksMap(MetricRegistry registry) {
        Map<String, HealthCheck> checks = new HashMap<>(3);
        checks.put(THRESHOLD_REQUESTS_CHECK, createTimeRequestsCheck(registry));
        return checks;
    }

    private Supplier<Double> valueFromGauge(MetricRegistry registry, String name) {

        // using deferred gauge resolving to allow health checks against the system with misconfigured metrics,
        // or Jetty not yet up during health check creation
        return () -> registry.timer(name).getOneMinuteRate();
    }
}
