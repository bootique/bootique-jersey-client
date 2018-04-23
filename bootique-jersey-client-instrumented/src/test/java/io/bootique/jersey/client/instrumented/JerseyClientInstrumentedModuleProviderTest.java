package io.bootique.jersey.client.instrumented;

import io.bootique.BQRuntime;
import io.bootique.jersey.client.JerseyClientModule;
import io.bootique.metrics.MetricsModule;
import io.bootique.metrics.health.HealthCheckModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class JerseyClientInstrumentedModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(JerseyClientInstrumentedModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new JerseyClientInstrumentedModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                JerseyClientModule.class,
                JerseyClientInstrumentedModule.class,
                MetricsModule.class,
                HealthCheckModule.class
        );
    }
}
