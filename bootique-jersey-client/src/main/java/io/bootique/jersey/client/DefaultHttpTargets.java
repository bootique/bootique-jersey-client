package io.bootique.jersey.client;

import javax.ws.rs.client.WebTarget;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @since 0.25
 */
public class DefaultHttpTargets implements HttpTargets {

    private Map<String, Supplier<WebTarget>> namedTargets;

    public DefaultHttpTargets(Map<String, Supplier<WebTarget>> namedTargets) {
        this.namedTargets = namedTargets;
    }

    @Override
    public WebTarget newTarget(String targetName) {
        return targetFactory(targetName).get();
    }

    protected Supplier<WebTarget> targetFactory(String name) {
        Supplier<WebTarget> supplier = namedTargets.get(name);

        if (supplier == null) {
            throw new IllegalArgumentException("No target configured for name: " + name);
        }

        return supplier;
    }
}
