package de.tobiasroeser.lambdatest.internal;

import java.util.*;

import de.tobiasroeser.lambdatest.F1;
import de.tobiasroeser.lambdatest.Optional;

/**
 * Some internally used helper methods, mostly copied from
 * de.tototec.utils.functional project.
 */
public class Util {

    public static <T, A extends T, B extends T> List<T> concat(final Iterable<A> first, final Iterable<B> second) {
        final LinkedList<T> result = new LinkedList<T>();
        for (final A a : first) {
            result.add(a);
        }
        for (final B b : second) {
            result.add(b);
        }
        return result;
    }

    public static String decapitalize(String string) {
        return string == null || string.isEmpty() ? "" : Character.toLowerCase(string.charAt(0)) + string.substring(1);
    }

    public static <T> List<T> distinct(final Iterable<T> source) {
        final List<T> result = new LinkedList<T>();
        for (final T t : source) {
            if (!result.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T> boolean exists(final Iterable<T> source, final F1<? super T, Boolean> exists) {
        for (final T t : source) {
            if (exists.apply(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<T> filterType(final Iterable<?> source, final Class<T> type) {
        final List<T> result = new LinkedList<T>();
        for (final Object object : source) {
            if (object != null && type.isAssignableFrom(object.getClass())) {
                @SuppressWarnings("unchecked") final T t = (T) object;
                result.add(t);
            }
        }
        return result;
    }

    public static <T> Optional<T> find(final Iterable<T> source,
                                       final F1<? super T, Boolean> accept) {
        for (final T t : source) {
            if (accept.apply(t)) {
                return Optional.some(t);
            }
        }
        return Optional.none();
    }

    public static <T, R> List<R> flatMap(final Iterable<T> source, final F1<? super T, ? extends Iterable<R>> convert) {
        final List<R> result = (source instanceof Collection<?>) ? new ArrayList<R>(((Collection<?>) source).size())
                : new LinkedList<R>();
        for (final T t : source) {
            final Iterable<R> subList = convert.apply(t);
            if (subList instanceof Collection<?>) {
                result.addAll((Collection<? extends R>) subList);
            } else {
                for (final R r : subList) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    public static <T> boolean forall(final Iterable<T> source, final F1<? super T, Boolean> forall) {
        for (final T t : source) {
            if (!forall.apply(t)) {
                return false;
            }
        }
        return true;
    }

    public static <T> void foreach(final Iterable<T> source, final Procedure1<? super T> foreach) {
        for (final T t : source) {
            foreach.apply(t);
        }
    }

    public static <T, K> Map<K, List<T>> groupBy(final Iterable<T> source, final F1<? super T, ? extends K> groupBy) {
        final Map<K, List<T>> result = new LinkedHashMap<K, List<T>>();
        for (final T t : source) {
            final K key = groupBy.apply(t);
            final List<T> list;
            if (result.containsKey(key)) {
                list = result.get(key);
            } else {
                list = new LinkedList<T>();
                result.put(key, list);
            }
            list.add(t);
        }
        return result;
    }

    public static <R, T> List<R> map(final Iterable<T> source, final F1<? super T, ? extends R> convert) {
        final List<R> result = (source instanceof Collection<?>) ? new ArrayList<R>(((Collection<?>) source).size())
                : new LinkedList<R>();
        for (final T t : source) {
            result.add(convert.apply(t));
        }
        return result;
    }

    public static <R, T> List<R> map(final T[] source, final F1<? super T, ? extends R> convert) {
        return map(Arrays.asList(source), convert);
    }

    public static String mkString(final Iterable<?> source, final String separator) {
        return mkString(source, null, separator, null);
    }

    public static String mkString(final Object[] source, final String separator) {
        return mkString(Arrays.asList(source), separator);
    }

    public static String mkString(final Iterable<?> source, final String prefix, final String separator,
                                  final String suffix) {
        return mkString(source, prefix, separator, suffix, null);
    }

    public static <T> String mkString(final T[] source, final String prefix, final String separator,
                                      final String suffix) {
        return mkString(Arrays.asList(source), prefix, separator, suffix);
    }

    public static <T> String mkString(final Iterable<T> source, final String prefix, final String separator,
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

    public static <T> String mkString(final T[] source, final String prefix, final String separator,
                                      final String suffix, final F1<? super T, String> convert) {
        return mkString(Arrays.asList(source), prefix, separator, suffix, convert);
    }

    public static <T> List<T> filter(final Iterable<T> source, final F1<? super T, Boolean> accept) {
        final List<T> result = new LinkedList<T>();
        for (final T t : source) {
            if (accept.apply(t)) {
                result.add(t);
            }
        }
        return result;
    }

}
