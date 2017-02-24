package io.bootique.jersey.client;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;

import javax.ws.rs.core.Feature;

/**
 * @since 0.9
 */
public class JerseyClientModuleExtender {

    private Binder binder;
    private Multibinder<Feature> features;

    JerseyClientModuleExtender(Binder binder) {
        this.binder = binder;
    }

    JerseyClientModuleExtender initAllExtensions() {
        contributeFeatures();
        return this;
    }


    public JerseyClientModuleExtender addFeature(Feature feature) {
        contributeFeatures().addBinding().toInstance(feature);
        return this;
    }

    public <T extends Feature> JerseyClientModuleExtender addFeature(Class<T> featureType) {
        contributeFeatures().addBinding().to(featureType);
        return this;
    }

    protected Multibinder<Feature> contributeFeatures() {

        if (features == null) {
            features = Multibinder.newSetBinder(binder, Feature.class, JerseyClientFeatures.class);
        }

        return features;
    }
}
