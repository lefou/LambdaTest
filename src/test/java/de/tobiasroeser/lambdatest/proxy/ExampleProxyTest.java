package de.tobiasroeser.lambdatest.proxy;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import java.util.Arrays;
import java.util.List;

import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExampleProxyTest extends FreeSpec {

	interface Dependency<T> {
		String hello();

		int foobar(String s1, int i2);

		T baz(List<T> arg1);
	}

	class ServiceWithDependency {
		private Dependency dependency;

		public ServiceWithDependency(final Dependency dependency) {
			this.dependency = dependency;
		}

		String usingDependency() {
			return dependency.hello();
		}

		String notUsingDependency() {
			return "Have a nice day!";
		}

	}

	interface Generic<T> {
		boolean genericArg(T arg1);

		T pass(T t1);
	}

	public ExampleProxyTest() {

		test("A proxy without delegates as optional dependencies should be sufficient", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class);
			final ServiceWithDependency service = new ServiceWithDependency(dep);
			expectEquals(service.notUsingDependency(), "Have a nice day!");
		});

		test("A proxy without delegates as mandatory dependencies should fail", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class);
			final ServiceWithDependency service = new ServiceWithDependency(dep);
			intercept(UnsupportedOperationException.class, "(?s).*public String hello\\(\\).*",
					() -> service.usingDependency());
		});

		test("A proxy with delegates as mandatory dependency should succeed", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
				@SuppressWarnings("unused")
				public String hello() {
					return "Hello Proxy!";
				}
			});
			final ServiceWithDependency service = new ServiceWithDependency(dep);
			expectEquals(service.usingDependency(), "Hello Proxy!");
		});

		test("A proxy handling an erased generic method should succeed", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
				@SuppressWarnings("unused")
				public Object baz(List<Object> arg1) {
					return arg1.iterator().next();
				}
			});

			expectEquals(dep.baz(Arrays.asList("1")), "1");
		});

		test("A Proxy handling a non-erased generic method should succeed", () -> {
			final Dependency<String> dep = TestProxy.proxy(Dependency.class, new Object() {
				@SuppressWarnings("unused")
				public String baz(List<String> arg1) {
					return arg1.iterator().next();
				}
			});

			expectEquals(dep.baz(Arrays.asList("1")), "1");
		});

		test("A proxy handing an erased generic arg method should succeed", () -> {
			final Generic<String> dep = TestProxy.proxy(Generic.class, new Object() {
				@SuppressWarnings("unused")
				public boolean genericArg(Object arg1) {
					return true;
				}
			});
			expectTrue(dep.genericArg("1"));
		});

		test("A proxy handing an non-erased generic arg method should fail", () -> {
			final Generic<String> dep = TestProxy.proxy(Generic.class, new Object() {
				@SuppressWarnings("unused")
				public boolean genericArg(String arg1) {
					return true;
				}
			});
			intercept(UnsupportedOperationException.class,
					"(?s).*\\Qpublic boolean genericArg(Object object1)\\E.*",
					() -> dep.genericArg("1"));
		});
		test("A proxy with missing implementation should print a nice (copy 'n paste -able) method signature",
				() -> {
					final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
						@SuppressWarnings("unused")
						public String hello() {
							return "Hello Proxy!";
						}
					});
					intercept(UnsupportedOperationException.class,
							"(?s).*\\Qpublic int foobar(String string1, int int1)\\E.*",
							() -> dep.foobar("a", 1));

					intercept(UnsupportedOperationException.class,
							"(?s).*\\Qpublic Object baz(List list1)\\E.*",
							() -> dep.baz(Arrays.asList("1")));

				});

	}

}
