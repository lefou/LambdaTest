package de.tobiasroeser.lambdatest.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tobiasroeser.lambdatest.F1;
import de.tobiasroeser.lambdatest.Optional;

/**
 * Utility class for simple mocking of interfaces.
 * <p>
 * The idea is, to isolate a class under test from its dependencies, by using
 * mocks or dummies as dependencies. As some interfaces are rather large and
 * contain many methods, implementing them for each test results in lots of
 * boilerplate code which is also hard to maintain.
 * <p>
 * This class provides an alternative way to easily create proxys with
 * {@link TestProxy#proxy}. You can either use the more explicit way with
 * {@link #proxy(ClassLoader, List, List)} or the more compact
 * {@link #proxy(Object...)}. With the optional Cloassloader, a new proxy clas
 * will be created which implements all the given interfaces. Additionally, you
 * can provide one ore more objects. Whenever a method is invoked on the proxy,
 * the given objects will be checked if they contain a method with a matching
 * signature, and if so, that method will be invoked an behalf of the proxy. If
 * er are no object(s) or no matching method was found, an
 * {@link UnsupportedOperationException} with a meaningful message will be
 * thrown.
 *
 */
public class TestProxy {

	public interface Option {
	}

	public static class IgnoreMethod implements Option {
		private final String name;
		private final Object defaultReturn;

		public IgnoreMethod(final String name, final Object defaultReturn) {
			this.name = name;
			this.defaultReturn = defaultReturn;
		}

		public String getName() {
			return name;
		}

		public Object getDefaultReturn() {
			return defaultReturn;
		}
	}

	public static IgnoreMethod ignoreMethod(final String name) {
		return new IgnoreMethod(name, null);
	}

	public static Optional<Tuple2<Object, Method>> findHandler(final List<Object> handlers, final Method method) {
		for (final Object handler : handlers) {
			try {
				final Method cand = handler.getClass().getMethod(method.getName(), method.getParameterTypes());
				return Optional.some(Tuple2.of(handler, cand));
			} catch (final NoSuchMethodException e) {
				continue;
			}
		}

		return Optional.none();
	}

	/**
	 * Creates a proxy object.
	 *
	 * @param classLoader
	 *            The classloader to load the interfaces.
	 * @param interfaces
	 *            Die Interfaces, die das Proxy-Objekt implementieren soll.
	 * @param delegates
	 *            The objects to which method-invocations of the proxy will be
	 *            delegated to.
	 * @return The invocation result of the delegated method calls, if found. If
	 *         no delegate was found, an exception.
	 *
	 * @throws UnsupportedOperationException
	 *             If no delegate method was found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T proxy(final ClassLoader classLoader, final List<Class<?>> interfaces,
			final List<Object> delegates, final List<Option> options) {

		return (T) Proxy.newProxyInstance(classLoader, interfaces.toArray(new Class<?>[0]), (proxy, method, args) -> {
			final Optional<IgnoreMethod> ignore = find(filterType(options, IgnoreMethod.class),
					i -> i.getName().equals(method.getName()));
			if (ignore.isDefined()) {
				return ignore.get().getDefaultReturn();
			}

			final Optional<Tuple2<Object, Method>> handler = findHandler(delegates, method);
			if (handler.isDefined()) {

				final Method m = handler.get().b();
				boolean resetAccessible = false;
				if (!m.isAccessible()) {
					resetAccessible = true;
					m.setAccessible(true);
				}
				try {
					final Object invokeReturn = m.invoke(handler.get().a(), args);
					return invokeReturn;
				} finally {
					if (resetAccessible) {
						m.setAccessible(false);
					}
				}
			} else if (method.getName().equals("toString") && args == null) {
				return "Proxy[" + mkString(interfaces, " & ") + "]@" + System.identityHashCode(proxy);
			} else {
				throw new UnsupportedOperationException(
						"Unhandled call: proxy=" + proxy + ", method=" + method + ", args="
								+ (args == null ? "null" : mkString(args, ",")));
			}
		});
	}

	/**
	 * Compact version of {@link #proxy(ClassLoader, List, List, List)}.
	 *
	 * @param classLoaderOrInterfaceOrDelegateOrOption
	 *            Variable set of parameters used the following way: 1) if
	 *            instance of {@link Option}, than used as option, 2) if
	 *            instance of ClassLoader, then used to create the proxy
	 *            instance, 3) if a class (no interface), used as delegate
	 *            object, 4) else it will be used as interface to be implemented
	 *            by the proxy.
	 *
	 * @see #proxy(ClassLoader, List, List, List)
	 */
	public static <T> T proxy(final Object... classLoaderOrInterfaceOrDelegateOrOption) {
		ClassLoader cl = null;
		final List<Class<?>> interfaces = new LinkedList<>();
		final List<Object> delegates = new LinkedList<>();
		final List<Option> options = new LinkedList<>();
		for (final Object object : classLoaderOrInterfaceOrDelegateOrOption) {
			if (object instanceof Option) {
				options.add((Option) object);
			} else if (object instanceof ClassLoader) {
				cl = (ClassLoader) object;
			} else if (object instanceof Class<?>) {
				interfaces.add((Class<?>) object);
			} else {
				delegates.add(object);
			}
		}
		return proxy(cl != null ? cl : TestProxy.class.getClassLoader(), interfaces, delegates, options);
	}

	// Utility methods below (copied from FList)

	private static <T> List<T> filterType(final Iterable<?> source, final Class<T> type) {
		final List<T> result = new LinkedList<T>();
		for (final Object object : source) {
			if (object != null && type.isAssignableFrom(object.getClass())) {
				@SuppressWarnings("unchecked")
				final T t = (T) object;
				result.add(t);
			}
		}
		return result;
	}

	private static <T> Optional<T> find(final Iterable<T> source, final F1<? super T, Boolean> accept) {
		for (final T t : source) {
			if (accept.apply(t)) {
				return Optional.some(t);
			}
		}
		return Optional.none();
	}

	private static String mkString(final Iterable<?> source, final String separator) {
		return mkString(source, null, separator, null, null);
	}

	private static String mkString(final Object[] source, final String separator) {
		return mkString(Arrays.asList(source), separator);
	}

	private static <T> String mkString(final Iterable<T> source, final String prefix, final String separator,
			final String suffix, final F1<? super T, String> convert) {
		final StringBuilder result = new StringBuilder();
		if (prefix != null) {
			result.append(prefix);
		}
		boolean sep = false;
		for (final T t : source) {
			if (sep && separator != null) {
				result.append(separator);
			}
			sep = true;
			if (convert != null) {
				result.append(convert.apply(t));
			} else {
				result.append(t == null ? null : t.toString());
			}
		}
		if (suffix != null) {
			result.append(suffix);
		}
		return result.toString();
	}

}
