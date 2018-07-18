package de.tobiasroeser.lambdatest.proxy;

import de.tobiasroeser.lambdatest.testng.FreeSpec;
import static de.tobiasroeser.lambdatest.Expect.expectEquals;

public class ExampleProxyTest extends FreeSpec {

	interface Dependency {
		String hello();

		int foobar(String s1, int i2);
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

	public ExampleProxyTest() {

		test("A proxy without delegates as optional dependencies should be sufficient", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class);
			final ServiceWithDependency service = new ServiceWithDependency(dep);
			expectEquals(service.notUsingDependency(), "Have a nice day!");
		});

		test("A proxy without delegates as mandatory dependencies should fail", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class);
			final ServiceWithDependency service = new ServiceWithDependency(dep);
			intercept(UnsupportedOperationException.class, "(?s).*public String hello\\(\\).*",() -> service.usingDependency());
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

		test("A proxy with with missing implementation should print a nice (copy 'n paste -able) method signature", () -> {
			final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
				@SuppressWarnings("unused")
				public String hello() {
					return "Hello Proxy!";
				}
			});
			intercept(UnsupportedOperationException.class, "(?s).*public int foobar\\(String string ,Integer integer\\).*",
					() -> dep.foobar("a",1));
		});

	}

}
