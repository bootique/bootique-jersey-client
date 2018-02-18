package io.bootique.jersey.client;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class JerseyClientModuleProviderIT {

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(JerseyClientModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(JerseyClientModuleProvider.class);
    }
}
