// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.func;

import java.util.Collection;
import java.util.Iterator;

/**
 * Functional operations on collections with generics support. The core interface is {@link Flow} to which operations
 * and transformations
 * (in terms of {@link Predicate}s, {@link Mapper}s and {@link Reducer}s) to create new Flows. Flows are initially
 * created
 * using {@link #flow(Collection)} and {@link #flow(Object...)}.
 * <p>
 * F will be used a bit, thus it has a short name (for those who don't like static imports). It provides a base set of
 * Predicate, Mapper and Reducer factories. A good development pattern for applications is to provide a similar,
 * application-specific, set of such factories.
 * 
 * @since 5.2.0
 */
@SuppressWarnings("all")
public class F
{
    final static Flow<?> EMPTY_FLOW = new EmptyFlow();

    @SuppressWarnings("unchecked")
    static <T> Flow<T> emptyFlow()
    {
        return (Flow<T>) EMPTY_FLOW;
    }

    public static <T> Predicate<T> eql(final T value)
    {
        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return object.equals(value);
            };
        };
    }

    public static Predicate<Number> eq(final long value)
    {
        return new Predicate<Number>()
        {
            public boolean accept(Number object)
            {
                return object.longValue() == value;
            }
        };
    }

    public static Predicate<Number> neq(long value)
    {
        return eq(value).invert();
    }

    public static Predicate<Number> gt(final long value)
    {
        return new Predicate<Number>()
        {
            public boolean accept(Number object)
            {
                return object.longValue() > value;
            }
        };
    }

    public static Predicate<Number> gteq(long value)
    {
        return eq(value).or(gt(value));
    }

    public static Predicate<Number> lt(long value)
    {
        return gteq(value).invert();
    }

    public static Predicate<Number> lteq(long value)
    {
        return gt(value).invert();
    }

    public static <T> Predicate<T> isNull()
    {
        return new Predicate<T>()
        {
            public boolean accept(T object)
            {
                return object == null;
            }
        };
    }

    public static <T> Predicate<T> notNull()
    {
        Predicate<T> isNull = isNull();

        return isNull.invert();
    }

    public static <T> Mapper<T, String> stringValueOf()
    {
        return new Mapper<T, String>()
        {
            public String map(T value)
            {
                return String.valueOf(value);
            };
        };
    }

    /** Returns a Mapper that ignores its input value and always returns a predetermined result. */
    public static <S, T> Mapper<S, T> always(final T fixedResult)
    {
        return new Mapper<S, T>()
        {
            public T map(S input)
            {
                return fixedResult;
            }
        };
    }

    /**
     * Mapper factory that combines a Predicate with two {@link Mapper}s; evaluating the predicate selects one of the
     * two mappers.
     * 
     * @param predicate
     *            evaluated to selected a coercion
     * @param ifAccepted
     *            used when predicate evaluates to true
     * @param ifRejected
     *            used when predicate evaluates to false
     */
    public static <S, T> Mapper<S, T> select(final Predicate<? super S> predicate, final Mapper<S, T> ifAccepted,
            final Mapper<S, T> ifRejected)
    {
        assert predicate != null;
        assert ifAccepted != null;
        assert ifRejected != null;

        return new Mapper<S, T>()
        {
            public T map(S input)
            {
                Mapper<S, T> active = predicate.accept(input) ? ifAccepted : ifRejected;

                return active.map(input);
            }
        };
    }

    /**
     * Override of {@link #select(Predicate, Mapper, Mapper)} where rejected values are replaced with null.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted)
    {
        return select(predicate, ifAccepted, (T) null);
    }

    /**
     * Override of {@link #select(Predicate, Mapper)} where rejected values are replaced with a fixed value.
     */
    public static <S, T> Mapper<S, T> select(Predicate<? super S> predicate, Mapper<S, T> ifAccepted, T ifRejectedValue)
    {
        Mapper<S, T> rejectedMapper = always(ifRejectedValue);

        return select(predicate, ifAccepted, rejectedMapper);
    }

    /** The identity mapper simply returns the input unchanged. */
    public static <S> Mapper<S, S> identity()
    {
        return new Mapper<S, S>()
        {
            public S map(S input)
            {
                return input;
            }
        };
    }

    /** Allows a Mapper that maps to boolean to be used as a Predicate. */
    public static <S> Predicate<S> toPredicate(final Mapper<S, Boolean> mapper)
    {
        assert mapper != null;

        return new Predicate<S>()
        {
            public boolean accept(S object)
            {
                return mapper.map(object);
            };
        };
    }

    public static Reducer<Integer, Integer> SUM_INTS = new Reducer<Integer, Integer>()
    {
        public Integer reduce(Integer accumulator, Integer value)
        {
            return accumulator + value;
        };
    };

    public static Mapper2<Integer, Integer, Integer> ADD_INTS = new Mapper2<Integer, Integer, Integer>()
    {
        public Integer map(Integer first, Integer second)
        {
            return first + second;
        };
    };

    /**
     * Extracts the values from the collection to form a {@link Flow}. The Collection
     * may change after the Flow is created without affecting the Flow.
     */
    public static <T> Flow<T> flow(Collection<T> values)
    {
        assert values != null;

        if (values.isEmpty())
            return emptyFlow();

        return new ArrayFlow<T>(values);
    }

    /**
     * Creates a new Flow from the values. You should not change the values array
     * after invoking this method (i.e., no defensive copy of the values is made).
     */
    public static <T> Flow<T> flow(T... values)
    {
        if (values.length == 0)
            return emptyFlow();

        return new ArrayFlow<T>(values);
    }

    /**
     * Creates a lazy Flow from the {@link Iterator} obtained from the iterable. The Flow
     * will be threadsafe as long as the iterable yields a new Iterator on each invocation <em>and</em> the underlying
     * iterable object is not modified while the Flow is evaluating. In other words, not extremely threadsafe.
     */
    public static <T> Flow<T> flow(Iterable<T> iterable)
    {
        assert iterable != null;

        return lazy(new LazyIterator<T>(iterable.iterator()));
    }

    /**
     * Creates a lazy Flow that returns integers in the given range. The range starts
     * with the lower value and counts by 1 up to the upper range (which is not part of
     * the Flow). If lower equals upper, the Flow is empty. If upper is less than lower,
     * the Flow counts down instead.
     * 
     * @param lower
     *            start of range (inclusive)
     * @param upper
     *            end of range (exclusive)
     */
    public static Flow<Integer> range(int lower, int upper)
    {
        if (lower == upper)
            return F.emptyFlow();

        if (lower < upper)
            return lazy(new LazyRange(lower, upper, 1));

        return lazy(new LazyRange(lower, upper, -1));
    }

    /**
     * Creates a {@link Flow} from a {@linkplain LazyFunction lazy function}.
     */
    public static <T> Flow<T> lazy(LazyFunction<T> function)
    {
        assert function != null;

        return new LazyFlow<T>(function);
    }

    /**
     * Creates an <em>infinite</em> series of numbers.
     * <p>
     * Attempting to get the {@linkplain Flow#count()} of the series will form an infinite loop.
     */
    public static Flow<Integer> series(int start, int delta)
    {
        return lazy(new LazySeries(start, delta));
    }

    /**
     * Creates a lazy, infinte Flow consisting of the initial value, then the result of passing
     * the initial value through the Mapper, and so forth, which each step value passed through the mapper
     * to form the next step value.
     */
    public static <T> Flow<T> iterate(final T initial, final Mapper<T, T> mapper)
    {
        assert mapper != null;

        return F.lazy(new LazyFunction<T>()
        {

            public LazyContinuation<T> next()
            {
                return new LazyContinuation<T>(initial, new LazyIterate<T>(initial, mapper));
            }
        });
    }

    public static <T> Worker<T> addToCollection(final Collection<T> coll)
    {
        return new Worker<T>()
        {
            public void work(T value)
            {
                coll.add(value);
            }
        };
    }
}
