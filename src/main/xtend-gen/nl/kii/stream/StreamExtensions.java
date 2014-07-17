package nl.kii.stream;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.Promise;
import nl.kii.stream.AsyncSubscription;
import nl.kii.stream.CommandSubscription;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.SyncSubscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class StreamExtensions {
  /**
   * create a stream of the given type
   */
  public static <T extends Object> Stream<T> stream(final Class<T> type) {
    return new Stream<T>();
  }
  
  public static <T extends Object> Stream<List<T>> streamList(final Class<T> type) {
    return new Stream<List<T>>();
  }
  
  public static <K extends Object, V extends Object> Stream<Map<K, V>> streamMap(final Pair<Class<K>, Class<V>> type) {
    return new Stream<Map<K, V>>();
  }
  
  /**
   * create a stream of a set of data and finish it
   */
  public static <T extends Object> Stream<T> stream(final T... data) {
    Iterator<T> _iterator = ((List<T>)Conversions.doWrapArray(data)).iterator();
    return StreamExtensions.<T>stream(_iterator);
  }
  
  /**
   * stream the data of a map as a list of key->value pairs
   */
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> stream(final Map<K, V> data) {
    Set<Map.Entry<K, V>> _entrySet = data.entrySet();
    final Function1<Map.Entry<K, V>, Pair<K, V>> _function = new Function1<Map.Entry<K, V>, Pair<K, V>>() {
      public Pair<K, V> apply(final Map.Entry<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        return Pair.<K, V>of(_key, _value);
      }
    };
    Iterable<Pair<K, V>> _map = IterableExtensions.<Map.Entry<K, V>, Pair<K, V>>map(_entrySet, _function);
    return StreamExtensions.<Pair<K, V>>stream(_map);
  }
  
  /**
   * stream an interable, ending with a finish
   */
  public static <T extends Object> Stream<T> stream(final Iterable<T> iterable) {
    Iterator<T> _iterator = iterable.iterator();
    return StreamExtensions.<T>stream(_iterator);
  }
  
  /**
   * stream an iterable, ending with a finish
   */
  public static <T extends Object> Stream<T> stream(final Iterator<T> iterator) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicBoolean finished = new AtomicBoolean(false);
      final Stream<T> stream = new Stream<T>();
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          boolean _hasNext = iterator.hasNext();
          if (_hasNext) {
            T _next = iterator.next();
            StreamExtensions.<T>operator_doubleGreaterThan(_next, stream);
          } else {
            boolean _get = finished.get();
            boolean _not = (!_get);
            if (_not) {
              finished.set(true);
              stream.finish();
            }
          }
        }
      };
      final Procedure0 pushNext = _function;
      final Procedure1<CommandSubscription> _function_1 = new Procedure1<CommandSubscription>() {
        public void apply(final CommandSubscription it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              pushNext.apply();
            }
          };
          it.onNext(_function);
        }
      };
      StreamExtensions.<T>monitor(stream, _function_1);
      pushNext.apply();
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Observe changes on the observable. This allows you to observe the stream
   * with multiple listeners. Observables do not support flow control, so every
   * value coming from the stream will be pushed out immediately.
   */
  public static <T extends Object> Observable<T> observe(final Stream<T> stream) {
    Publisher<T> _xblockexpression = null;
    {
      final Publisher<T> publisher = new Publisher<T>();
      StreamExtensions.<T>onEach(stream, publisher);
      _xblockexpression = publisher;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <T extends Object> Stream<T> operator_doubleGreaterThan(final T value, final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      stream.push(value);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <T extends Object> Stream<T> operator_doubleLessThan(final Stream<T> stream, final T value) {
    Stream<T> _xblockexpression = null;
    {
      stream.push(value);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <T extends Object> Stream<T> operator_doubleGreaterThan(final List<T> value, final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          stream.push(it);
        }
      };
      IterableExtensions.<T>forEach(value, _function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <T extends Object> Stream<T> operator_doubleLessThan(final Stream<T> stream, final List<T> value) {
    Stream<T> _xblockexpression = null;
    {
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          stream.push(it);
        }
      };
      IterableExtensions.<T>forEach(value, _function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add an entry to a stream (such as error or finish)
   */
  public static <T extends Object> Stream<T> operator_doubleLessThan(final Stream<T> stream, final Entry<T> entry) {
    Stream<T> _xblockexpression = null;
    {
      stream.apply(entry);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the << operator
   */
  public static <T extends Object> Stream<T> operator_doubleLessThan(final Stream<T> stream, final Throwable t) {
    Stream<T> _xblockexpression = null;
    {
      nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(t);
      stream.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the >> operator
   */
  public static <T extends Object> Stream<T> operator_doubleGreaterThan(final Throwable t, final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      nl.kii.stream.Error<T> _error = new nl.kii.stream.Error<T>(t);
      stream.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <T extends Object> Finish<T> finish() {
    return new Finish<T>(0);
  }
  
  public static <T extends Object> Finish<T> finish(final int level) {
    return new Finish<T>(level);
  }
  
  static <T extends Object, R extends Object> CommandSubscription controls(final Stream<T> newStream, final AsyncSubscription<?> parent) {
    final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
      public void apply(final CommandSubscription it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.next();
          }
        };
        it.onNext(_function);
        final Procedure1<Void> _function_1 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.skip();
          }
        };
        it.onSkip(_function_1);
        final Procedure1<Void> _function_2 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.close();
          }
        };
        it.onClose(_function_2);
      }
    };
    return StreamExtensions.<T>monitor(newStream, _function);
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn
   */
  public static <T extends Object, R extends Object> Stream<R> map(final Stream<T> stream, final Function1<? super T, ? extends R> mappingFn) {
    Stream<R> _xblockexpression = null;
    {
      final Stream<R> newStream = new Stream<R>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              final R mapped = mappingFn.apply(it);
              newStream.push(mapped);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<R, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn.
   * Also passes a counter to count the amount of items passed since
   * the start or the last finish.
   */
  public static <T extends Object, R extends Object> Stream<R> map(final Stream<T> stream, final Function2<? super T, ? super Long, ? extends R> mappingFn) {
    Stream<R> _xblockexpression = null;
    {
      final AtomicLong counter = new AtomicLong(0);
      final Stream<R> newStream = new Stream<R>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              long _incrementAndGet = counter.incrementAndGet();
              final R mapped = mappingFn.apply(it, Long.valueOf(_incrementAndGet));
              newStream.push(mapped);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                counter.set(0);
              }
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<R, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <T extends Object> Stream<T> filter(final Stream<T> stream, final Function1<? super T, ? extends Boolean> filterFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = filterFn.apply(it);
              if ((_apply).booleanValue()) {
                newStream.push(it);
              } else {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Splits a stream into multiple parts. These parts are separated by Finish entries.
   * Streams support multiple levels of finishes, to indicate multiple levels of splits.
   * This allows you to split a stream, and then split it again.
   * <p>
   * It follows these rules:
   * <ul>
   * <li>when a new split is applied, it is always at finish level 0
   * <li>all other stream operations that use finish, always use this level
   * <li>the existing splits are all upgraded a level. so a level 0 finish becomes a level 1 finish
   * <li>splits at a higher level always are carried through to a lower level. so wherever there is a
   *     level 1 split for example, there is also a level 0 split
   * </ul>
   * <p>
   * The reason for these seemingly strange rules is that it allows us to split and merge a stream
   * multiple times consistently. For example, consider the following stream, where finish(x) represents
   * a finish of level x:
   * <pre>
   * val s = (1..10).stream
   * // this stream contains:
   * 1, 2, 3, 4, 5, 6, 7, finish(0)
   * // the finish(0) is automatically added by the iterator as it ends
   * </pre>
   * If we split this stream at 4, we get this:
   * <pre>
   * val s2 = s.split[it==4]
   * // s2 now contains:
   * 1, 2, 3, 4, finish(0), 5, 6, 7, finish(0), finish(1)
   * </pre>
   * The split had as condition that it would occur at it==4, so after 4 a finish(0) was added.
   * Also, the finish(0) at the end became upgraded to finish(1), and because a splits at
   * a higher level always punch through, it also added a finish(0).
   * <p>
   * In the same manner we can keep splitting the stream and each time we will add another layer
   * of finishes.
   * <p>
   * We can also merge the stream and this will reverse the process, reducing one level:
   * <pre>
   * val s3 = s2.merge
   * // s3 now contains:
   * 1, 2, 3, 4, 5, 6, 7, finish(0)
   * </pre>
   * <p>
   * We can also merge by calling collect, which will transform the data between the splits into lists.
   * The reason why splits of higher levels cut into the splits of lower levels is that the split levels
   * are not independant. The finishes let you model a stream of stream. What essentially is simulated is:
   * <pre>
   * val s = int.stream
   * val Stream<Stream<Integer>> s2 = s.split
   * // and merge reverses:
   * val Stream<Integer> s3 = s2.merge
   * </pre>
   * There are several reasons why this library does not use this substream approach:
   * <ul>
   * <li>Streams of streams are uncertain in their behavior, it is not guaranteed that this stream is serial or parallel.
   * <li>Streams are not light objects, having queues, and having streams of streams would be memory and performance expensive
   * <li>Streams of streams are not easily serializable and cannot easliy be throught of as a linear stream
   * <li>Streams of streams are harder to reason and program with than a single stream
   * </ul>
   * <p>
   * However the split and merge commands are there to simulate having substreams. To think of it more simply like a
   * List<List<T>>, you cannot have a separation at the higher list level, which is not represented at the <List<T>> level.
   */
  public static <T extends Object> Stream<T> split(final Stream<T> stream, final Function1<? super T, ? extends Boolean> splitConditionFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final AtomicBoolean justPostedFinish0 = new AtomicBoolean(false);
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = splitConditionFn.apply(it);
              if ((_apply).booleanValue()) {
                Value<T> _value = new Value<T>(it);
                Finish<T> _finish = new Finish<T>(0);
                final List<? extends Entry<T>> entries = Collections.<Entry<T>>unmodifiableList(CollectionLiterals.<Entry<T>>newArrayList(_value, _finish));
                justPostedFinish0.set(true);
                Entries<T> _entries = new Entries<T>(((Entry<T>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              } else {
                justPostedFinish0.set(false);
                Value<T> _value_1 = new Value<T>(it);
                newStream.apply(_value_1);
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              boolean _get = justPostedFinish0.get();
              if (_get) {
                Finish<Object> _finish = new Finish<Object>((it.level + 1));
                newStream.apply(_finish);
              } else {
                justPostedFinish0.set(true);
                Finish<Object> _finish_1 = new Finish<Object>(0);
                Finish<Object> _finish_2 = new Finish<Object>((it.level + 1));
                final List<Finish<Object>> entries = Collections.<Finish<Object>>unmodifiableList(CollectionLiterals.<Finish<Object>>newArrayList(_finish_1, _finish_2));
                Entries<Object> _entries = new Entries<Object>(((Entry<Object>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              }
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Merges one level of finishes.
   */
  public static <T extends Object> Stream<T> merge(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Value<T> _value = new Value<T>(it);
              newStream.apply(_value);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level > 0)) {
                newStream.finish((it.level - 1));
              } else {
                stream.next();
              }
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Only let pass a certain amount of items through the stream
   */
  public static <T extends Object> Stream<T> limit(final Stream<T> stream, final int amount) {
    final Function2<T, Long, Boolean> _function = new Function2<T, Long, Boolean>() {
      public Boolean apply(final T it, final Long c) {
        return Boolean.valueOf(((c).longValue() > amount));
      }
    };
    return StreamExtensions.<T>until(stream, _function);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <T extends Object> Stream<T> until(final Stream<T> stream, final Function1<? super T, ? extends Boolean> untilFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = untilFn.apply(it);
              if ((_apply).booleanValue()) {
                stream.skip();
                stream.next();
              } else {
                newStream.push(it);
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * Passes a counter as second parameter to the untilFn, starting at 1.
   */
  public static <T extends Object> Stream<T> until(final Stream<T> stream, final Function2<? super T, ? super Long, ? extends Boolean> untilFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicLong count = new AtomicLong(0);
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              long _incrementAndGet = count.incrementAndGet();
              Boolean _apply = untilFn.apply(it, Long.valueOf(_incrementAndGet));
              if ((_apply).booleanValue()) {
                stream.skip();
                stream.next();
              } else {
                newStream.push(it);
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              count.set(0);
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * It only asks the next promise from the stream when the previous promise has been resolved.
   */
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<Promise<T>> stream) {
    return StreamExtensions.<T, Object>resolve(stream, 1);
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * <p>
   * Allows concurrency promises to be resolved in parallel.
   * <p>
   * note: resolving breaks flow control.
   */
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<Promise<T>> stream, final int concurrency) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final AtomicBoolean isFinished = new AtomicBoolean(false);
      final AtomicInteger processes = new AtomicInteger(0);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          final int open = processes.decrementAndGet();
          boolean _get = isFinished.get();
          if (_get) {
            newStream.finish();
          }
          if ((concurrency > open)) {
            stream.next();
          }
        }
      };
      final Procedure0 onProcessComplete = _function;
      final Procedure1<AsyncSubscription<Promise<T>>> _function_1 = new Procedure1<AsyncSubscription<Promise<T>>>() {
        public void apply(final AsyncSubscription<Promise<T>> it) {
          final Procedure1<Promise<T>> _function = new Procedure1<Promise<T>>() {
            public void apply(final Promise<T> promise) {
              processes.incrementAndGet();
              final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
                public void apply(final Throwable it) {
                  newStream.error(it);
                  stream.next();
                }
              };
              Promise<T> _onError = promise.onError(_function);
              final Procedure1<T> _function_1 = new Procedure1<T>() {
                public void apply(final T it) {
                  newStream.push(it);
                  onProcessComplete.apply();
                }
              };
              _onError.then(_function_1);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
              stream.next();
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<Promise<T>>> _function_2 = new Procedure1<Finish<Promise<T>>>() {
            public void apply(final Finish<Promise<T>> it) {
              int _get = processes.get();
              boolean _equals = (_get == 0);
              if (_equals) {
                newStream.finish(it.level);
                stream.next();
              } else {
                isFinished.set(true);
              }
            }
          };
          it.finish(_function_2);
        }
      };
      StreamExtensions.<Promise<T>>onAsync(stream, _function_1);
      stream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * note: onEach swallows exceptions in your listener. If you needs error detection/handling, use .on[] instead.
   */
  public static <T extends Object> void onEach(final Stream<T> stream, final Procedure1<? super T> listener) {
    final Procedure1<SyncSubscription<T>> _function = new Procedure1<SyncSubscription<T>>() {
      public void apply(final SyncSubscription<T> it) {
        it.each(listener);
        final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            try {
              throw it;
            } catch (Throwable _e) {
              throw Exceptions.sneakyThrow(_e);
            }
          }
        };
        it.error(_function);
      }
    };
    StreamExtensions.<T>on(stream, _function);
  }
  
  /**
   * Asynchronous listener to the stream. It will create an AsyncSubscription which you use to control the stream.
   * By just calling this to listen, values will not arrive. Instead, you need to call next on the subscription
   * to ask for the next message.
   * <p>
   * Note that this is a very manual way of listening, usually you are better off by creating asynchronous methods
   * and mapping to these methods.
   */
  public static <T extends Object> AsyncSubscription<T> onEachAsync(final Stream<T> stream, final Procedure2<? super T, ? super AsyncSubscription<T>> listener) {
    final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
      public void apply(final AsyncSubscription<T> sub) {
        final Procedure1<T> _function = new Procedure1<T>() {
          public void apply(final T it) {
            listener.apply(it, sub);
          }
        };
        sub.each(_function);
        final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            try {
              throw it;
            } catch (Throwable _e) {
              throw Exceptions.sneakyThrow(_e);
            }
          }
        };
        sub.error(_function_1);
      }
    };
    return StreamExtensions.<T>onAsync(stream, _function);
  }
  
  /**
   * Forward the results of the stream to another stream and start that stream.
   */
  public static <T extends Object> void forwardTo(final Stream<T> stream, final Stream<T> otherStream) {
    final Procedure1<SyncSubscription<T>> _function = new Procedure1<SyncSubscription<T>>() {
      public void apply(final SyncSubscription<T> it) {
        final Procedure1<T> _function = new Procedure1<T>() {
          public void apply(final T it) {
            otherStream.push(it);
          }
        };
        it.each(_function);
        final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            otherStream.error(it);
          }
        };
        it.error(_function_1);
        final Procedure1<Finish<T>> _function_2 = new Procedure1<Finish<T>>() {
          public void apply(final Finish<T> it) {
            otherStream.finish();
          }
        };
        it.finish(_function_2);
      }
    };
    StreamExtensions.<T>on(stream, _function);
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   */
  public static <T extends Object> Promise<T> first(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.set(it);
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.error(it);
              }
            }
          };
          it.error(_function_1);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      subscription.next();
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Start the stream and listen to the first value only.
   */
  public static <T extends Object> void then(final Stream<T> stream, final Procedure1<T> listener) {
    Promise<T> _first = StreamExtensions.<T>first(stream);
    _first.then(listener);
  }
  
  public static <T extends Object> AsyncSubscription<T> onError(final Stream<T> stream, final Procedure1<? super Throwable> listener) {
    final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
      public void apply(final AsyncSubscription<T> subscription) {
        final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            listener.apply(it);
            subscription.next();
          }
        };
        subscription.error(_function);
      }
    };
    return StreamExtensions.<T>onAsync(stream, _function);
  }
  
  public static <T extends Object> AsyncSubscription<T> onFinish(final Stream<T> stream, final Procedure1<? super Finish<T>> listener) {
    final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
      public void apply(final AsyncSubscription<T> subscription) {
        final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
          public void apply(final Finish<T> it) {
            listener.apply(it);
            subscription.next();
          }
        };
        subscription.finish(_function);
      }
    };
    return StreamExtensions.<T>onAsync(stream, _function);
  }
  
  public static <T extends Object> AsyncSubscription<T> onError(final AsyncSubscription<T> subscription, final Procedure1<? super Throwable> listener) {
    AsyncSubscription<T> _xblockexpression = null;
    {
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          listener.apply(it);
          subscription.next();
        }
      };
      subscription.error(_function);
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> AsyncSubscription<T> onFinish(final AsyncSubscription<T> subscription, final Procedure1<? super Finish<T>> listener) {
    AsyncSubscription<T> _xblockexpression = null;
    {
      final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
        public void apply(final Finish<T> it) {
          listener.apply(it);
          subscription.next();
        }
      };
      subscription.finish(_function);
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> SyncSubscription<T> on(final Stream<T> stream, final Procedure1<? super SyncSubscription<T>> subscriptionFn) {
    SyncSubscription<T> _xblockexpression = null;
    {
      final SyncSubscription<T> subscription = new SyncSubscription<T>(stream);
      subscriptionFn.apply(subscription);
      stream.next();
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> AsyncSubscription<T> onAsync(final Stream<T> stream, final Procedure1<? super AsyncSubscription<T>> subscriptionFn) {
    AsyncSubscription<T> _xblockexpression = null;
    {
      final AsyncSubscription<T> subscription = new AsyncSubscription<T>(stream);
      subscriptionFn.apply(subscription);
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> CommandSubscription monitor(final Stream<T> stream, final Procedure1<? super CommandSubscription> subscriptionFn) {
    CommandSubscription _xblockexpression = null;
    {
      final CommandSubscription handler = new CommandSubscription(stream);
      subscriptionFn.apply(handler);
      _xblockexpression = handler;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> void onEach(final AsyncSubscription<T> subscription, final Procedure1<? super T> listener) {
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        listener.apply(it);
        subscription.next();
      }
    };
    subscription.each(_function);
    subscription.next();
  }
  
  public static <T extends Object> void onEachAsync(final AsyncSubscription<T> subscription, final Procedure2<? super T, ? super AsyncSubscription<T>> listener) {
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        listener.apply(it, subscription);
      }
    };
    subscription.each(_function);
    subscription.next();
  }
  
  /**
   * Collect all items from a stream, separated by finishes
   */
  public static <T extends Object> Stream<List<T>> collect(final Stream<T> stream) {
    Stream<List<T>> _xblockexpression = null;
    {
      LinkedList<T> _linkedList = new LinkedList<T>();
      final AtomicReference<LinkedList<T>> list = new AtomicReference<LinkedList<T>>(_linkedList);
      final Stream<List<T>> newStream = new Stream<List<T>>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              LinkedList<T> _get = list.get();
              _get.add(it);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                final LinkedList<T> collected = list.get();
                LinkedList<T> _linkedList = new LinkedList<T>();
                list.set(_linkedList);
                newStream.push(collected);
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<List<T>, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add the value of all the items in the stream until a finish.
   */
  public static <T extends Number> Stream<Double> sum(final Stream<T> stream) {
    Stream<Double> _xblockexpression = null;
    {
      final AtomicDouble sum = new AtomicDouble(0);
      final Stream<Double> newStream = new Stream<Double>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              double _doubleValue = it.doubleValue();
              sum.addAndGet(_doubleValue);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                final double collected = sum.doubleValue();
                sum.set(0);
                newStream.push(Double.valueOf(collected));
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<Double, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Average the items in the stream until a finish
   */
  public static <T extends Number> Stream<Double> average(final Stream<T> stream) {
    Stream<Double> _xblockexpression = null;
    {
      final AtomicDouble avg = new AtomicDouble();
      final AtomicLong count = new AtomicLong(0);
      final Stream<Double> newStream = new Stream<Double>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              double _doubleValue = it.doubleValue();
              avg.addAndGet(_doubleValue);
              count.incrementAndGet();
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                double _doubleValue = avg.doubleValue();
                long _andSet = count.getAndSet(0);
                final double collected = (_doubleValue / _andSet);
                avg.set(0);
                newStream.push(Double.valueOf(collected));
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<Double, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Count the number of items passed in the stream until a finish.
   */
  public static <T extends Object> Stream<Long> count(final Stream<T> stream) {
    Stream<Long> _xblockexpression = null;
    {
      final AtomicLong count = new AtomicLong(0);
      final Stream<Long> newStream = new Stream<Long>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              count.incrementAndGet();
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                long _andSet = count.getAndSet(0);
                newStream.push(Long.valueOf(_andSet));
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<Long, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value until a finish.
   */
  public static <T extends Object> Stream<T> reduce(final Stream<T> stream, final T initial, final Function2<? super T, ? super T, ? extends T> reducerFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> reduced = new AtomicReference<T>(initial);
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              T _get = reduced.get();
              T _apply = reducerFn.apply(_get, it);
              reduced.set(_apply);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                T _andSet = reduced.getAndSet(initial);
                newStream.push(_andSet);
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * The counter is the count of the incoming stream entry (since the start or the last finish)
   */
  public static <T extends Object> Stream<T> reduce(final Stream<T> stream, final T initial, final Function3<? super T, ? super T, ? super Long, ? extends T> reducerFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> reduced = new AtomicReference<T>(initial);
      final AtomicLong count = new AtomicLong(0);
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              T _get = reduced.get();
              long _andIncrement = count.getAndIncrement();
              T _apply = reducerFn.apply(_get, it, Long.valueOf(_andIncrement));
              reduced.set(_apply);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                final T result = reduced.getAndSet(initial);
                count.set(0);
                newStream.push(result);
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * True if any of the values match the passed testFn
   * FIX: currently .first gives recursive loop?
   */
  public static <T extends Object> Stream<Boolean> anyMatch(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final Stream<Boolean> newStream = new Stream<Boolean>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = testFn.apply(it);
              if ((_apply).booleanValue()) {
                anyMatch.set(true);
                newStream.push(Boolean.valueOf(true));
                stream.skip();
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Finish<T>> _function_1 = new Procedure1<Finish<T>>() {
            public void apply(final Finish<T> it) {
              if ((it.level == 0)) {
                final boolean matched = anyMatch.get();
                anyMatch.set(false);
                if ((!matched)) {
                  newStream.push(Boolean.valueOf(false));
                }
              } else {
                newStream.finish((it.level - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<Boolean, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
}
