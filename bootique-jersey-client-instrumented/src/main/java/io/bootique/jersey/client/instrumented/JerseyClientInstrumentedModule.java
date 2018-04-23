package io.bootique.jersey.client.instrumented;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.jersey.client.JerseyClientModule;

public class JerseyClientInstrumentedModule extends ConfigModule {

    @Override
    public void configure(Binder binder) {
        JerseyClientModule.extend(binder).addFeature(InstrumentedFeature.class);
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
}
