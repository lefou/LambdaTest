package de.tobiasroeser.lambdatest.proxy;

import static de.tobiasroeser.lambdatest.Expect.expectEquals;
import static de.tobiasroeser.lambdatest.Expect.expectTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tobiasroeser.lambdatest.Expect;
import de.tobiasroeser.lambdatest.testng.FreeSpec;

public class ExampleProxyTest extends FreeSpec {

	interface Dependency<U> {
		String hello();

		int foobar(String s1, int i2);

		U baz(List<U> arg1);

		String list(List<String> strings);
		<T> T generic(T t);
		<T> T generics1(List<T> ts);

		<T, S> T generics2(List<S> ts);

		<T, S> T generics3(Map<S, List<T>> ts);
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
					"(?s).*\\Qpublic boolean genericArg(Object object0)\\E.*",
					() -> dep.genericArg("1"));
		});

		test("A proxy handling generics works with proposed code templates",() -> {
			final Dependency<String> proxy = TestProxy.proxy(Dependency.class, new Object() {
				// ==> public <T> T generic(T x0){}
				public <T> T generic(T x0){ return x0; }

				// ==> public U baz(List<U> x0){}
				// works: public <X> X baz(List<X> x0){ return x0.get(0);}
				// works: public String baz(List<String> x0){ return x0.get(0);}
				// does not work public  int baz(List<Integer> x0){ return x0.get(0);}
				public <X> X baz(List<X> x0){ return x0.get(0);}

			});
			Expect.expectString(proxy.generic("abc")).isEqual("abc");
			Expect.expectString(proxy.baz(Arrays.asList("abc"))).contains("abc");
		});

		section("A proxy with with missing implementation should print a nice (copy 'n paste -able) method signature",
				() -> {
					test("case: int foobar(String s1, int i2)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public int foobar\\(String string0 ,int int1\\).*",
										() -> dep.foobar("a", 1));
							});
					test("case: U baz(List<U> arg1)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public U baz\\(List<U> list0\\).*",
										() -> dep.baz(new ArrayList()));
							});
					test("case: String list(List<String> strings)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public String list\\(List<String> list0\\).*",
										() -> dep.list(new ArrayList<>()));
							});

					test("case: <T> T generics1(List<T> ts)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public <T> T generics1\\(List<T> list0\\).*",
										() -> dep.generics1(new ArrayList<String>()));
							});
					test("case: <T, S> T generics2(List<S> ts)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public <T, S> T generics2\\(List<S> list0\\).*",
										() -> dep.generics2(new ArrayList<String>()));
							});

					test("case: <T, S> T generics3(Map<S,List<T>> ts)",
							() -> {
								final Dependency dep = TestProxy.proxy(Dependency.class, new Object() {
									@SuppressWarnings("unused")
									public String hello() {
										return "Hello Proxy!";
									}
								});
								intercept(UnsupportedOperationException.class,
										"(?s).*public <T, S> T generics3\\(Map<S, List<T>> map0\\).*",
										() -> {
											dep.generics3(new LinkedHashMap<String, List<String>>());
										});
							});
				});
	}

}
