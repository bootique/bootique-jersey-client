/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jersey.client;

import com.google.inject.Injector;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class ClientGuiceBridgeFeature implements Feature {

	private static final String INJECTOR_PROPERTY = "io.bootique.jersey.client.injector";

	// TODO: can all of this happen inside this Feature "configure(..)" method?
	static void register(ClientConfig config, Injector injector) {
		config.property(ClientGuiceBridgeFeature.INJECTOR_PROPERTY, injector);
		config.register(ClientGuiceBridgeFeature.class);
	}

	static Injector getInjector(Configuration configuration) {
		Injector injector = (Injector) configuration.getProperty(ClientGuiceBridgeFeature.INJECTOR_PROPERTY);
		if (injector == null) {
			throw new IllegalStateException("Injector is not available in JAX RS runtime. Use property '"
					+ ClientGuiceBridgeFeature.INJECTOR_PROPERTY + "' to set it");
		}

		return injector;
	}

	@Override
	public boolean configure(FeatureContext context) {

		context.register(new AbstractBinder() {

			@Override
			protected void configure() {

				Injector injector = ClientGuiceBridgeFeature.getInjector(context.getConfiguration());
				ClientGuiceInjectInjector injectInjector = new ClientGuiceInjectInjector(injector);

				bind(injectInjector).to(new TypeLiteral<InjectionResolver<com.google.inject.Inject>>() {
				});
			}
		});

		return true;
	}
}
