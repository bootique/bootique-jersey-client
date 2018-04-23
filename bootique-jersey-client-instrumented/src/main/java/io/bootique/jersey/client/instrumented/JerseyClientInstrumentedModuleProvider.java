package io.bootique.jersey.client.instrumented;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jersey.client.JerseyClientModule;
import io.bootique.jersey.client.JerseyClientModuleProvider;
import io.bootique.metrics.MetricsModuleProvider;
import io.bootique.metrics.health.HealthCheckModuleProvider;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

public class JerseyClientInstrumentedModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JerseyClientInstrumentedModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(JerseyClientModule.class);
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JerseyClientModuleProvider(),
                new HealthCheckModuleProvider(),
                new MetricsModuleProvider()
        );
    }
}
