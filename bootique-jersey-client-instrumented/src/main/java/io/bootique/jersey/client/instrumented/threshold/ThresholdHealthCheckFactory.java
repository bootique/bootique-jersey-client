package io.bootique.jersey.client.instrumented.threshold;

import com.codahale.metrics.MetricRegistry;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.instrumented.JerseyClientInstrumentedModule;
import io.bootique.metrics.MetricNaming;
import io.bootique.metrics.health.HealthCheck;
import io.bootique.metrics.health.check.DoubleRangeFactory;
import io.bootique.metrics.health.check.ValueRange;
import io.bootique.metrics.health.check.ValueRangeCheck;

import java.util.function.Supplier;

/**
 * @since 0.26
 */
@BQConfig
public class ThresholdHealthCheckFactory {

    private DoubleRangeFactory timeRequestsThresholds;
    private MetricRegistry metricRegistry;

    public ThresholdHealthCheckFactory initMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        return this;
    }

    @BQConfigProperty
    public void setTimeRequestsThresholds(DoubleRangeFactory timeRequestsThresholds) {
        this.timeRequestsThresholds = timeRequestsThresholds;
    }

    public RangeHealthCheck createThresholdHealthCheck() {
        return new RangeHealthCheck(createTimeRequestsCheck(getMetricRegistry()));
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    private HealthCheck createTimeRequestsCheck(MetricRegistry registry) {
        Supplier<Double> deferredGauge = valueFromGauge(registry, MetricNaming.forModule(JerseyClientInstrumentedModule.class).name("Client", "RequestTimer"));

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

    private Supplier<Double> valueFromGauge(MetricRegistry registry, String name) {

        // using deferred gauge resolving to allow health checks against the system with misconfigured metrics,
        // or Jetty not yet up during health check creation
        return () -> registry.timer(name).getOneMinuteRate();
    }
}
