package io.bootique.jersey.client;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;

import javax.ws.rs.core.Feature;
import java.util.Set;

public class JerseyClientModule extends ConfigModule {

    /**
     * Returns an instance of {@link JerseyClientModuleExtender} used by downstream modules to load custom extensions of
     * services declared in the JerseyClientModule. Should be invoked from a downstream Module's "configure" method.
     *
     * @param binder DI binder passed to the Module that invokes this method.
     * @return an instance of {@link JerseyClientModuleExtender} that can be used to load JerseyClientModule extensions.
     * @since 0.9
     */
    public static JerseyClientModuleExtender extend(Binder binder) {
        return new JerseyClientModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        extend(binder).initAllExtensions();
    }

    @Provides
    @Singleton
    HttpClientFactoryFactory provideClientFactoryFactory(ConfigurationFactory configurationFactory) {
        return configurationFactory.config(HttpClientFactoryFactory.class, configPrefix);
    }

    @Provides
    @Singleton
    HttpClientFactory provideClientFactory(
            HttpClientFactoryFactory factoryFactory,
            Injector injector,
            @JerseyClientFeatures Set<Feature> features) {
        
        return factoryFactory.createClientFactory(injector, features);
    }

    @Provides
    @Singleton
    HttpTargets provideTargets(HttpClientFactoryFactory factoryFactory, HttpClientFactory clientFactory) {
        return factoryFactory.createTargets(clientFactory);
    }
}
