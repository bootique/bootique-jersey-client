package io.bootique.jersey.client;

import java.lang.reflect.Member;
import java.util.Objects;

import javax.inject.Singleton;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Annotations;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

// even though we are binding ClientGuiceInjectInjector as an instance, HK2 requires @Singleton annotation here...
@Singleton
public class ClientGuiceInjectInjector implements InjectionResolver<Inject> {

	private Injector injector;

	public ClientGuiceInjectInjector(Injector injector) {
		this.injector = Objects.requireNonNull(injector);
	}

	@Override
	public boolean isConstructorParameterIndicator() {
		return true;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return false;
	}

	@Override
	public Object resolve(Injectee injectee, ServiceHandle<?> serviceHandle) {

		if (injectee.getRequiredType() instanceof Class) {

			TypeLiteral<?> typeLiteral = TypeLiteral.get(injectee.getRequiredType());
			Errors errors = new Errors(injectee.getParent());
			Key<?> key;
			try {
				key = Annotations.getKey(typeLiteral, (Member) injectee.getParent(),
						injectee.getParent().getDeclaredAnnotations(), errors);
			} catch (ErrorsException e) {
				errors.merge(e.getErrors());
				throw new ConfigurationException(errors.getMessages());
			}

			return injector.getInstance(key);
		}

		throw new IllegalStateException("Can't process injection point: " + injectee.getRequiredType());
	}

}
