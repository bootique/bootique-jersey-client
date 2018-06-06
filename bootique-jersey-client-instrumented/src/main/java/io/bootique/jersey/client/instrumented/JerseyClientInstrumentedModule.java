package io.bootique.jersey.client.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jersey.client.JerseyClientModule;
import io.bootique.jersey.client.instrumented.threshold.JerseyHealthChecks;
import io.bootique.jersey.client.instrumented.threshold.ThresholdHealthCheckFactory;
import io.bootique.metrics.MetricNaming;
import io.bootique.metrics.health.HealthCheckModule;

public class JerseyClientInstrumentedModule extends ConfigModule {

    public static final MetricNaming METRIC_NAMING = MetricNaming.forModule(JerseyClientInstrumentedModule.class);


    public JerseyClientInstrumentedModule () {
        super("jerseyClientInstrumented");
    }

    @Override
    public void configure(Binder binder) {

        JerseyClientModule.extend(binder).addFeature(InstrumentedFeature.class);
        HealthCheckModule.extend(binder).addHealthCheckGroup(JerseyHealthChecks.class);

    }

    @Provides
    @Singleton
    InstrumentedFeature provideTimingFeature(ClientTimingFilter filter) {
        return new InstrumentedFeature(filter);
    }

    @Provides
    @Singleton
    ClientTimingFilter provideTimingFilter(MetricRegistry metricRegistry) {
        return new ClientTimingFilter(metricRegistry);
    }

    @Provides
    ThresholdHealthCheckFactory providerThresholdHealthCheckFactory(ConfigurationFactory configFactory) {
        return configFactory.config(ThresholdHealthCheckFactory.class, configPrefix).initMetricRegistry();
    }

    @Provides
    @Singleton
    JerseyHealthChecks provideThresholdHealthCheck(ThresholdHealthCheckFactory health, MetricRegistry metricRegistry) {
        return health.createThresholdHealthCheck(metricRegistry);
    }

}
