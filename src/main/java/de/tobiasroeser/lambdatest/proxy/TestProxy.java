package de.tobiasroeser.lambdatest.proxy;

import static de.tobiasroeser.lambdatest.internal.Util.decapitalize;
import static de.tobiasroeser.lambdatest.internal.Util.exists;
import static de.tobiasroeser.lambdatest.internal.Util.filterType;
import static de.tobiasroeser.lambdatest.internal.Util.find;
import static de.tobiasroeser.lambdatest.internal.Util.map;
import static de.tobiasroeser.lambdatest.internal.Util.mkString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tobiasroeser.lambdatest.Optional;
import de.tobiasroeser.lambdatest.internal.LoggerFactory;

/**
 * Utility class for simple mocking of interfaces.
 *
 * The idea is, to isolate a class under test from its dependencies, by using
 * mocks or dummies as dependencies. As some interfaces are rather large and
 * contain many methods, implementing them for each test results in lots of
 * boilerplate code which is also hard to maintain.
 *
 * This class provides an alternative way to easily create proxys with the
 * `proxy`-methods. You can either use the more explicit way with
 * {@link #proxy(ClassLoader, List, List, List)} or the more compact
 * {@link #proxy(Object[]) proxy(Object..)} A new proxy class will be created
 * (using the optionally given ClassLoader) which implements all the given
 * interfaces. Additionally, you can provide one ore more delegate objects.
 * Whenever a method is invoked on the proxy, the given objects will be checked
 * if they contain a method with a matching signature, and if so, that method
 * will be invoked an behalf of the proxy. If there are no object(s) or no
 * matching method was found, an {@link UnsupportedOperationException} with a
 * meaningful message will be thrown.
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
	 * @return The invocation result of the delegated method calls, if found. If no
	 *         delegate was found, an exception.
	 *
	 * @throws UnsupportedOperationException
	 *             If no delegate method was found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T proxy(final ClassLoader classLoader, final List<Class<?>> interfaces,
			final List<Object> delegates, final List<Option> options) {

		return (T) Proxy.newProxyInstance(classLoader, interfaces.toArray(new Class<?>[0]), (proxy, method, args) -> {
			final String methodName = method.getName();
			final Optional<IgnoreMethod> ignore = find(filterType(options, IgnoreMethod.class),
					i -> i.getName().equals(methodName));
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
				} catch (final InvocationTargetException e) {
					LoggerFactory.getLogger(TestProxy.class)
							.debug("The invoked method [" + method + "] of proxy " + "Proxy["
									+ mkString(interfaces, " & ") + "]@"
									+ System.identityHashCode(proxy) + " throw an exception", e.getCause());
					// the underlying method throw an exception, which we simply
					// pass through
					throw e.getCause();
				} finally {
					if (resetAccessible) {
						m.setAccessible(false);
					}
				}
			} else if (methodName.equals("toString") && args == null) {
				return "Proxy[" + mkString(interfaces, " & ") + "]@" + System.identityHashCode(proxy);
			} else {
				final String methodSignature = methodSignature(method);

				final String optionalMethodSignature = hasTypeParameter(interfaces)
						? "\nOR ==>  " + methodSignatureWithoutGenerics(method) + " { ... } "
						: "";

				throw new UnsupportedOperationException(
						"Unhandled call: proxy=" + proxy + ", method=" + method + ", args=" +
								(args == null ? "null" : mkString(args, ", ")) +
								"\nTo handle this call in the proxy delegate object, " +
								"add a method with the following signature:" +
								"\n   ==>  " + methodSignature + " { ... }  " +
								optionalMethodSignature +
								"\n");
			}
		});
	}

	private static boolean hasTypeParameter(List<Class<?>> interfaces) {
		return exists(interfaces, i -> i.getTypeParameters().length > 0);
	}

	private static String methodSignatureWithoutGenerics(final Method method) {
		final Class<?>[] types = method.getParameterTypes();
		final String argList;
    final Map<String, Integer> count = new LinkedHashMap<>();
    final List<String> args = map(types, t -> {
      final String className = t.getSimpleName();
      final String argName = decapitalize(className).replace("[]","");
      Integer c = count.get(argName);
      c = c == null ? 1 : c + 1;
      count.put(argName, c);
      return className + " " + argName + c;
    });
    argList = mkString(args, ", ");
    final String methodName = method.getName();
		final String returnTypeName = method.getReturnType().getSimpleName();

		return "public " + returnTypeName + " " + methodName + "(" + argList + ")";
	}

	private static String methodSignature(final Method method) {
		final String argList = mkArgListString(method);
		final String methodName = method.getName();
		final String returnTypeName = getTypeName(method.getGenericReturnType());
		final TypeVariable<Method>[] typeParameters = method.getTypeParameters();
		final String typeVariables = typeParameters == null || typeParameters.length == 0 ? ""
				: mkTypeVariablesList(typeParameters) + " ";

		return "public " + typeVariables + returnTypeName + " " + methodName + "(" + argList + ")";
	}

	private static String mkTypeVariablesList(TypeVariable<Method>[] typeParameters) {
		return "<" + mkString(map(typeParameters, t -> getTypeName(t)), ", ") + ">";
	}

	private static String getTypeName(Type type) {
		if (type instanceof Class<?>) {
			return ((Class<?>) type).getSimpleName();
		} else {
			return type.toString().replaceAll("[a-zA-Z0-1_]+\\.", "");
		}
	}

	private static String mkArgListString(Method method) {

		final Type[] parameterTypes = method.getGenericParameterTypes();
		final Class<?>[] parameterClasses = method.getParameterTypes();

		if (parameterTypes == null) {
			return "";
		}

		final Map<String, Integer> count = new LinkedHashMap<>();

		final List<String> argsWithClassAndParameterName = new LinkedList<>();
		for (int i = 0; i < parameterTypes.length; i++) {
			final Type arg = parameterTypes[i];
			final Class<?> clazz = parameterClasses[i];

			final boolean isErasedOrObject = Object.class.equals(clazz);

			final String clazzSimpleName = clazz.getSimpleName();
			final String argName = decapitalize(clazzSimpleName).replace("[]", "");
			final String className = isErasedOrObject ? "Object" : getTypeName(arg);

			Integer c = count.get(argName);
			c = c == null ? 1 : c + 1;
			count.put(argName, c);

			argsWithClassAndParameterName.add(className + " " + argName + c);
		}
		return mkString(argsWithClassAndParameterName, " ,");
	}

	/**
	 * Compact version of {@link #proxy(ClassLoader, List, List, List)}.
	 *
	 * @param classLoaderOrInterfaceOrDelegateOrOption
	 *            Variable set of parameters used the following way: 1) if instance
	 *            of {@link Option}, than used as option, 2) if instance of
	 *            ClassLoader, then used to create the proxy instance, 3) if a class
	 *            (no interface), used as delegate object, 4) else it will be used
	 *            as interface to be implemented by the proxy.
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

}
