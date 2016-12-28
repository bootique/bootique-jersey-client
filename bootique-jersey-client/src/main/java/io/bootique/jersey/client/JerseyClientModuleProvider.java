package io.bootique.jersey.client;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

import java.util.Collections;
import java.util.Map;

public class JerseyClientModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new JerseyClientModule();
    }

    @Override
    public Map<String, Class<?>> configs() {
        // TODO: config prefix is hardcoded. Refactor away from ConfigModule, and make provider
        // generate config prefix, reusing it in metadata...
        return Collections.singletonMap("jerseyclient", HttpClientFactoryFactory.class);
    }
}
