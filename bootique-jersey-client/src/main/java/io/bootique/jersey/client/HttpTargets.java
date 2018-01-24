package io.bootique.jersey.client;

import javax.ws.rs.client.WebTarget;

/**
 * An injectable manager of preconfigured JAX RS {@link WebTarget} objects.
 *
 * @since 0.25
 */
public interface HttpTargets {

    /**
     * Returns a new {@link WebTarget} object associated with a named configuration, that can be used to send
     * requests to a given HTTP endpoint. This method allows to delegate HTTP endpoint configuration to the Bootique
     * configuration subsystem instead of doing it in the code.
     *
     * @param targetName a symbolic name of the returned target associated with configuration under
     *                   "jerseyclient.targets".
     * @return a new {@link WebTarget} associated with a named configuration, that can be used to send requests to a
     * given HTTP endpoint.
     * @since 0.25
     */
    WebTarget newTarget(String targetName);
}
