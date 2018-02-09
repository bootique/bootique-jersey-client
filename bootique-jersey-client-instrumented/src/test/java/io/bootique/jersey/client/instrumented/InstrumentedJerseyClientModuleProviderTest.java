package io.bootique.jersey.client.instrumented;

import io.bootique.BQRuntime;
import io.bootique.jersey.client.JerseyClientModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.collect.ImmutableList.of;

public class InstrumentedJerseyClientModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(InstrumentedJerseyClientModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new InstrumentedJerseyClientModuleProvider()).createRuntime();
        BQModuleProviderChecker.testModulesLoaded(bqRuntime, of(
                JerseyClientModule.class,
                InstrumentedJerseyClientModule.class,
                MetricsModule.class,
                HealthCheckModule.class
        ));
    }
}
