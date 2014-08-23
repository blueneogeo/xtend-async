package nl.kii.stream;

import com.google.common.base.Objects;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AtomicDouble;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Async;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;
import nl.kii.stream.CommandSubscription;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.Subscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure3;

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
   * create a stream of pairs
   */
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> streamPair(final Pair<Class<K>, Class<V>> type) {
    return new Stream<Pair<K, V>>();
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
          boolean _get = finished.get();
          if (_get) {
            return;
          }
          boolean _hasNext = iterator.hasNext();
          if (_hasNext) {
            T _next = iterator.next();
            StreamExtensions.<T>operator_doubleGreaterThan(_next, stream);
          } else {
            finished.set(true);
            stream.finish();
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              finished.set(true);
              stream.finish();
            }
          };
          it.onSkip(_function_1);
        }
      };
      StreamExtensions.<T>monitor(stream, _function_1);
      pushNext.apply();
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * stream a standard Java inputstream. closing the stream closes the inputstream.
   */
  public static Stream<List<Byte>> stream(final InputStream stream) {
    try {
      Stream<List<Byte>> _xblockexpression = null;
      {
        final Stream<List<Byte>> newStream = new Stream<List<Byte>>();
        ByteStreams.<Object>readBytes(stream, new ByteProcessor() {
          public Object getResult() {
            Object _xblockexpression = null;
            {
              newStream.finish();
              _xblockexpression = null;
            }
            return _xblockexpression;
          }
          
          public boolean processBytes(final byte[] buf, final int off, final int len) throws IOException {
            boolean _xblockexpression = false;
            {
              Boolean _open = newStream.getOpen();
              boolean _not = (!(_open).booleanValue());
              if (_not) {
                return false;
              }
              newStream.push(((List<Byte>)Conversions.doWrapArray(buf)));
              _xblockexpression = true;
            }
            return _xblockexpression;
          }
        });
        final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
          public void apply(final CommandSubscription it) {
            final Procedure1<Void> _function = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.onSkip(_function);
            final Procedure1<Void> _function_1 = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.onClose(_function_1);
          }
        };
        StreamExtensions.<List<Byte>>monitor(newStream, _function);
        _xblockexpression = newStream;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * stream a file as byte blocks. closing the stream closes the file.
   */
  public static Stream<List<Byte>> stream(final File file) {
    try {
      Stream<List<Byte>> _xblockexpression = null;
      {
        final ByteSource source = Files.asByteSource(file);
        BufferedInputStream _openBufferedStream = source.openBufferedStream();
        _xblockexpression = StreamExtensions.stream(_openBufferedStream);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * create an unending stream of random integers in the range you have given
   */
  public static Stream<Integer> randomStream(final IntegerRange range) {
    Stream<Integer> _xblockexpression = null;
    {
      final Random randomizer = new Random();
      final Stream<Integer> newStream = StreamExtensions.<Integer>stream(int.class);
      final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
        public void apply(final CommandSubscription it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              int _start = range.getStart();
              int _size = range.getSize();
              int _nextInt = randomizer.nextInt(_size);
              final int next = (_start + _nextInt);
              newStream.push(Integer.valueOf(next));
            }
          };
          it.onNext(_function);
        }
      };
      StreamExtensions.<Integer>monitor(newStream, _function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a publisher for the stream. This allows you to observe the stream
   * with multiple listeners. Publishers do not support flow control, and the
   * created Publisher will eagerly pull all data from the stream for publishing.
   */
  public static <T extends Object> Publisher<T> publish(final Stream<T> stream) {
    Publisher<T> _xblockexpression = null;
    {
      final Publisher<T> publisher = new Publisher<T>();
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              publisher.apply(it);
              Boolean _publishing = publisher.getPublishing();
              if ((_publishing).booleanValue()) {
                stream.next();
              }
            }
          };
          it.each(_function);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.next();
      _xblockexpression = publisher;
    }
    return _xblockexpression;
  }
  
  /**
   * Create new streams from an observable. Notice that these streams are
   * being pushed only, you lose flow control. Closing the stream will also
   * unsubscribe from the observable.
   */
  public static <T extends Object> Stream<T> stream(final Observable<T> observable) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          newStream.push(it);
        }
      };
      final Procedure0 stopObserving = observable.onChange(_function);
      final Procedure1<CommandSubscription> _function_1 = new Procedure1<CommandSubscription>() {
        public void apply(final CommandSubscription it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              stopObserving.apply();
            }
          };
          it.onClose(_function);
        }
      };
      StreamExtensions.<T>monitor(newStream, _function_1);
      _xblockexpression = newStream;
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
  
  static <T extends Object, R extends Object> CommandSubscription controls(final Stream<T> newStream, final Subscription<?> parent) {
    final Procedure1<CommandSubscription> _function = new Procedure1<CommandSubscription>() {
      public void apply(final CommandSubscription it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.stream.next();
          }
        };
        it.onNext(_function);
        final Procedure1<Void> _function_1 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.stream.skip();
          }
        };
        it.onSkip(_function_1);
        final Procedure1<Void> _function_2 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.stream.close();
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<R, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Perform mapping of a pair stream using a function that exposes the key and value of
   * the incoming value.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Stream<V2> map(final Stream<Pair<K1, V1>> stream, final Function2<? super K1, ? super V1, ? extends V2> mappingFn) {
    final Function1<Pair<K1, V1>, V2> _function = new Function1<Pair<K1, V1>, V2>() {
      public V2 apply(final Pair<K1, V1> it) {
        K1 _key = it.getKey();
        V1 _value = it.getValue();
        return mappingFn.apply(_key, _value);
      }
    };
    return StreamExtensions.<Pair<K1, V1>, V2>map(stream, _function);
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <T extends Object> Stream<T> filter(final Stream<T> stream, final Function1<? super T, ? extends Boolean> filterFn) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return filterFn.apply(it);
      }
    };
    return StreamExtensions.<T>filter(stream, _function);
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <T extends Object> Stream<T> filter(final Stream<T> stream, final Function3<? super T, ? super Long, ? super Long, ? extends Boolean> filterFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              final long i = index.incrementAndGet();
              long _get = passed.get();
              Boolean _apply = filterFn.apply(it, Long.valueOf(i), Long.valueOf(_get));
              if ((_apply).booleanValue()) {
                passed.incrementAndGet();
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
              index.set(0);
              passed.set(0);
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> filter(final Stream<Pair<K, V>> stream, final Function2<? super K, ? super V, ? extends Boolean> filterFn) {
    final Function1<Pair<K, V>, Boolean> _function = new Function1<Pair<K, V>, Boolean>() {
      public Boolean apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        return filterFn.apply(_key, _value);
      }
    };
    return StreamExtensions.<Pair<K, V>>filter(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Merges one level of finishes.
   * @See StreamExtensions.split
   */
  public static <T extends Object> Stream<T> merge(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return untilFn.apply(it);
      }
    };
    return StreamExtensions.<T>until(stream, _function);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <T extends Object> Stream<T> until(final Stream<T> stream, final Function3<? super T, ? super Long, ? super Long, ? extends Boolean> untilFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              final long i = index.incrementAndGet();
              long _get = passed.get();
              Boolean _apply = untilFn.apply(it, Long.valueOf(i), Long.valueOf(_get));
              if ((_apply).booleanValue()) {
                passed.incrementAndGet();
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
              index.set(0);
              passed.set(0);
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<? extends IPromise<T>> stream) {
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
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<? extends IPromise<T>> stream, final int concurrency) {
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
      final Procedure1<Subscription<? extends IPromise<T>>> _function_1 = new Procedure1<Subscription<? extends IPromise<T>>>() {
        public void apply(final Subscription<? extends IPromise<T>> it) {
          final Procedure1<IPromise<T>> _function = new Procedure1<IPromise<T>>() {
            public void apply(final IPromise<T> promise) {
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
          final Procedure1<Finish<? extends IPromise<T>>> _function_2 = new Procedure1<Finish<? extends IPromise<T>>>() {
            public void apply(final Finish<? extends IPromise<T>> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.on(stream, _function_1);
      stream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Resolves the value promise of the stream into just the value.
   * Only a single promise will be resolved at once.
   */
  public static <K extends Object, V extends Object, P extends IPromise<V>> Stream<Pair<K, V>> resolveValue(final Stream<Pair<K, P>> stream) {
    return StreamExtensions.<K, V, P>resolveValue(stream, 1);
  }
  
  /**
   * Resolves the value promise of the stream into just the value.
   * [concurrency] promises will be resolved at once (at the most).
   */
  public static <K extends Object, V extends Object, P extends IPromise<V>> Stream<Pair<K, V>> resolveValue(final Stream<Pair<K, P>> stream, final int concurrency) {
    Stream<Pair<K, V>> _xblockexpression = null;
    {
      final Stream<Pair<K, V>> newStream = new Stream<Pair<K, V>>();
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
      final Procedure1<Subscription<Pair<K, P>>> _function_1 = new Procedure1<Subscription<Pair<K, P>>>() {
        public void apply(final Subscription<Pair<K, P>> it) {
          final Procedure1<Pair<K, P>> _function = new Procedure1<Pair<K, P>>() {
            public void apply(final Pair<K, P> result) {
              final K key = result.getKey();
              final P promise = result.getValue();
              processes.incrementAndGet();
              final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
                public void apply(final Throwable it) {
                  newStream.error(it);
                  stream.next();
                }
              };
              Promise<V> _onError = promise.onError(_function);
              final Procedure1<V> _function_1 = new Procedure1<V>() {
                public void apply(final V it) {
                  Pair<K, V> _mappedTo = Pair.<K, V>of(key, it);
                  newStream.push(_mappedTo);
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
          final Procedure1<Finish<Pair<K, P>>> _function_2 = new Procedure1<Finish<Pair<K, P>>>() {
            public void apply(final Finish<Pair<K, P>> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<Pair<K, P>>on(stream, _function_1);
      stream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<T> stream, final Function1<? super T, ? extends P> promiseFn) {
    Stream<P> _map = StreamExtensions.<T, P>map(stream, promiseFn);
    return StreamExtensions.<R, Object>resolve(_map);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<T> stream, final int concurrency, final Function1<? super T, ? extends P> promiseFn) {
    Stream<P> _map = StreamExtensions.<T, P>map(stream, promiseFn);
    return StreamExtensions.<R, Object>resolve(_map, concurrency);
  }
  
  /**
   * Make an asynchronous call. Passes the key and value of the pair stream as separate parameters.
   * This is an alias for stream.map(mappingFn).resolve
   */
  public static <K extends Object, T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<Pair<K, T>> stream, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    Stream<P> _map = StreamExtensions.<K, T, P>map(stream, promiseFn);
    return StreamExtensions.<R, Object>resolve(_map);
  }
  
  /**
   * Make an asynchronous call. Passes the key and value of the pair stream as separate parameters.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <K extends Object, T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<Pair<K, T>> stream, final int concurrency, final Function2<? super K, ? super T, ? extends P> promiseFn) {
    Stream<P> _map = StreamExtensions.<K, T, P>map(stream, promiseFn);
    return StreamExtensions.<R, Object>resolve(_map, concurrency);
  }
  
  public static <T extends Object, R extends Object, P extends IPromise<R>, K2 extends Object> Stream<Pair<K2, R>> call2(final Stream<T> stream, final Function1<? super T, ? extends Pair<K2, P>> promiseFn) {
    Stream<Pair<K2, P>> _map = StreamExtensions.<T, Pair<K2, P>>map(stream, promiseFn);
    return StreamExtensions.<K2, R, P>resolveValue(_map);
  }
  
  public static <T extends Object, R extends Object, P extends IPromise<R>, K2 extends Object> Stream<Pair<K2, R>> call2(final Stream<T> stream, final int concurrency, final Function1<? super T, ? extends Pair<K2, P>> promiseFn) {
    Stream<Pair<K2, P>> _map = StreamExtensions.<T, Pair<K2, P>>map(stream, promiseFn);
    return StreamExtensions.<K2, R, P>resolveValue(_map, concurrency);
  }
  
  public static <K extends Object, T extends Object, R extends Object, P extends IPromise<R>, K2 extends Object> Stream<Pair<K2, R>> call2(final Stream<Pair<K, T>> stream, final Function2<? super K, ? super T, ? extends Pair<K2, P>> promiseFn) {
    Stream<Pair<K2, P>> _map = StreamExtensions.<K, T, Pair<K2, P>>map(stream, promiseFn);
    return StreamExtensions.<K2, R, P>resolveValue(_map);
  }
  
  public static <K extends Object, T extends Object, R extends Object, P extends IPromise<R>, K2 extends Object> Stream<Pair<K2, R>> call2(final Stream<Pair<K, T>> stream, final int concurrency, final Function2<? super K, ? super T, ? extends Pair<K2, P>> promiseFn) {
    Stream<Pair<K2, P>> _map = StreamExtensions.<K, T, Pair<K2, P>>map(stream, promiseFn);
    return StreamExtensions.<K2, R, P>resolveValue(_map, concurrency);
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * note: onEach swallows exceptions in your listener. If you needs error detection/handling, use .on[] instead.
   */
  public static <T extends Object> Task onEach(final Stream<T> stream, final Procedure1<? super T> listener) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        try {
          it.printStackTrace();
          throw it;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    Subscription<T> _onError = StreamExtensions.<T>onError(stream, _function);
    return StreamExtensions.<T>onEach(_onError, listener);
  }
  
  /**
   * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
   */
  public static <K extends Object, V extends Object> Task onEach(final Stream<Pair<K, V>> stream, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    return StreamExtensions.<Pair<K, V>>onEach(stream, _function);
  }
  
  /**
   * Performs a task for every incoming value.
   */
  public static <T extends Object> Task onEachAsync(final Stream<T> stream, final Function1<? super T, ? extends Task> listener) {
    Stream<Task> _map = StreamExtensions.<T, Task>map(stream, listener);
    Stream<Boolean> _resolve = StreamExtensions.<Boolean, Object>resolve(_map);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
      }
    };
    return StreamExtensions.<Boolean>onEach(_resolve, _function);
  }
  
  /**
   * Forward the results of the stream to another stream and start that stream.
   */
  public static <T extends Object> void forwardTo(final Stream<T> stream, final Stream<T> otherStream) {
    final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
      public void apply(final Subscription<T> it) {
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
        final Procedure0 _function_3 = new Procedure0() {
          public void apply() {
            otherStream.close();
          }
        };
        it.closed(_function_3);
      }
    };
    final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
    StreamExtensions.<T, Object>controls(otherStream, subscription);
    subscription.stream.next();
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   */
  public static <T extends Object> IPromise<T> first(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      subscription.stream.next();
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Will keep asking on the stream until it gets to the last value.
   */
  public static <T extends Object> IPromise<T> last(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final AtomicReference<T> last = new AtomicReference<T>();
      final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
        public void apply(final Finish<T> it) {
          boolean _and = false;
          Boolean _fulfilled = promise.getFulfilled();
          boolean _not = (!(_fulfilled).booleanValue());
          if (!_not) {
            _and = false;
          } else {
            T _get = last.get();
            boolean _notEquals = (!Objects.equal(_get, null));
            _and = _notEquals;
          }
          if (_and) {
            T _get_1 = last.get();
            promise.set(_get_1);
          }
        }
      };
      Subscription<T> _onFinish = StreamExtensions.<T>onFinish(stream, _function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          Boolean _fulfilled = promise.getFulfilled();
          boolean _not = (!(_fulfilled).booleanValue());
          if (_not) {
            promise.error(it);
          }
        }
      };
      Subscription<T> _onError = StreamExtensions.<T>onError(_onFinish, _function_1);
      final Procedure1<T> _function_2 = new Procedure1<T>() {
        public void apply(final T it) {
          Boolean _fulfilled = promise.getFulfilled();
          boolean _not = (!(_fulfilled).booleanValue());
          if (_not) {
            last.set(it);
          }
        }
      };
      StreamExtensions.<T>onEach(_onError, _function_2);
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Skip an amount of items from the stream, and process only the ones after that.
   * Resets at finish.
   */
  public static <T extends Object> Stream<T> skip(final Stream<T> stream, final int amount) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    return StreamExtensions.<T>filter(stream, _function);
  }
  
  /**
   * Take only a set amount of items from the stream.
   * Resets at finish.
   */
  public static <T extends Object> Stream<T> take(final Stream<T> stream, final int amount) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    return StreamExtensions.<T>until(stream, _function);
  }
  
  /**
   * Start the stream and and promise the first value from it.
   */
  public static <T extends Object> void then(final Stream<T> stream, final Procedure1<T> listener) {
    IPromise<T> _first = StreamExtensions.<T>first(stream);
    _first.then(listener);
  }
  
  public static <T extends Object> Subscription<T> onClosed(final Stream<T> stream, final Procedure1<? super Stream<T>> listener) {
    final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
      public void apply(final Subscription<T> subscription) {
        final Procedure0 _function = new Procedure0() {
          public void apply() {
            listener.apply(stream);
            Stream<T> _stream = subscription.getStream();
            _stream.next();
          }
        };
        subscription.closed(_function);
      }
    };
    return StreamExtensions.<T>on(stream, _function);
  }
  
  public static <T extends Object> Subscription<T> onError(final Stream<T> stream, final Procedure1<? super Throwable> listener) {
    final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
      public void apply(final Subscription<T> subscription) {
        final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
          public void apply(final Throwable it) {
            listener.apply(it);
            subscription.stream.next();
          }
        };
        subscription.error(_function);
      }
    };
    return StreamExtensions.<T>on(stream, _function);
  }
  
  public static <T extends Object> Subscription<T> onFinish(final Stream<T> stream, final Procedure1<? super Finish<T>> listener) {
    final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
      public void apply(final Subscription<T> subscription) {
        final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
          public void apply(final Finish<T> it) {
            listener.apply(it);
            subscription.stream.next();
          }
        };
        subscription.finish(_function);
      }
    };
    return StreamExtensions.<T>on(stream, _function);
  }
  
  public static <T extends Object> Subscription<T> onError(final Subscription<T> subscription, final Procedure1<? super Throwable> listener) {
    Subscription<T> _xblockexpression = null;
    {
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          listener.apply(it);
          subscription.stream.next();
        }
      };
      subscription.error(_function);
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Subscription<T> onFinish(final Subscription<T> subscription, final Procedure1<? super Finish<T>> listener) {
    Subscription<T> _xblockexpression = null;
    {
      final Procedure1<Finish<T>> _function = new Procedure1<Finish<T>>() {
        public void apply(final Finish<T> it) {
          listener.apply(it);
          subscription.stream.next();
        }
      };
      subscription.finish(_function);
      _xblockexpression = subscription;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Subscription<T> on(final Stream<T> stream, final Procedure1<? super Subscription<T>> subscriptionFn) {
    Subscription<T> _xblockexpression = null;
    {
      final Subscription<T> subscription = new Subscription<T>(stream);
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
  
  public static <T extends Object> Task onEach(final Subscription<T> subscription, final Procedure1<? super T> listener) {
    Task _xblockexpression = null;
    {
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          listener.apply(it);
          subscription.stream.next();
        }
      };
      subscription.each(_function);
      subscription.stream.next();
      _xblockexpression = subscription.toTask();
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Task onEachAsync(final Subscription<T> subscription, final Procedure2<? super T, ? super Subscription<T>> listener) {
    Task _xblockexpression = null;
    {
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          listener.apply(it, subscription);
        }
      };
      subscription.each(_function);
      subscription.stream.next();
      _xblockexpression = subscription.toTask();
    }
    return _xblockexpression;
  }
  
  public static <K extends Object, V extends Object> Task onEach(final Subscription<Pair<K, V>> subscription, final Procedure2<? super K, ? super V> listener) {
    Task _xblockexpression = null;
    {
      final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
        public void apply(final Pair<K, V> it) {
          K _key = it.getKey();
          V _value = it.getValue();
          listener.apply(_key, _value);
          subscription.stream.next();
        }
      };
      subscription.each(_function);
      subscription.stream.next();
      _xblockexpression = subscription.toTask();
    }
    return _xblockexpression;
  }
  
  public static <K extends Object, V extends Object> Task onEachAsync(final Subscription<Pair<K, V>> subscription, final Procedure3<? super K, ? super V, ? super Subscription<Pair<K, V>>> listener) {
    Task _xblockexpression = null;
    {
      final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
        public void apply(final Pair<K, V> it) {
          K _key = it.getKey();
          V _value = it.getValue();
          listener.apply(_key, _value, subscription);
        }
      };
      subscription.each(_function);
      subscription.stream.next();
      _xblockexpression = subscription.toTask();
    }
    return _xblockexpression;
  }
  
  /**
   * Opposite of collect, separate each list in the stream into separate
   * stream entries and streams those separately.
   */
  public static <T extends Object> Stream<T> separate(final Stream<List<T>> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Subscription<List<T>>> _function = new Procedure1<Subscription<List<T>>>() {
        public void apply(final Subscription<List<T>> it) {
          final Procedure1<List<T>> _function = new Procedure1<List<T>>() {
            public void apply(final List<T> list) {
              final Function1<T, Value<T>> _function = new Function1<T, Value<T>>() {
                public Value<T> apply(final T it) {
                  return new Value<T>(it);
                }
              };
              final List<Value<T>> entries = ListExtensions.<T, Value<T>>map(list, _function);
              Entries<T> _entries = new Entries<T>(((Entry<T>[])Conversions.unwrapArray(entries, Entry.class)));
              newStream.apply(_entries);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Finish<List<T>>> _function_2 = new Procedure1<Finish<List<T>>>() {
            public void apply(final Finish<List<T>> it) {
              newStream.finish(it.level);
            }
          };
          it.finish(_function_2);
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<List<T>> subscription = StreamExtensions.<List<T>>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
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
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * True if any of the values match the passed testFn
   */
  public static <T extends Object> Stream<Boolean> anyMatch(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final Stream<Boolean> newStream = new Stream<Boolean>();
      final Procedure1<Subscription<T>> _function = new Procedure1<Subscription<T>>() {
        public void apply(final Subscription<T> it) {
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
          final Procedure0 _function_3 = new Procedure0() {
            public void apply() {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      final Subscription<T> subscription = StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<Boolean, Object>controls(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static Stream<String> toText(final Stream<List<Byte>> stream) {
    return StreamExtensions.toText(stream, "UTF-8");
  }
  
  public static Stream<String> toText(final Stream<List<Byte>> stream, final String encoding) {
    final Function1<List<Byte>, List<String>> _function = new Function1<List<Byte>, List<String>>() {
      public List<String> apply(final List<Byte> it) {
        try {
          String _string = new String(((byte[])Conversions.unwrapArray(it, byte.class)), encoding);
          String[] _split = _string.split("\n");
          return IterableExtensions.<String>toList(((Iterable<String>)Conversions.doWrapArray(_split)));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    Stream<List<String>> _map = StreamExtensions.<List<Byte>, List<String>>map(stream, _function);
    return StreamExtensions.<String>separate(_map);
  }
  
  public static Stream<List<Byte>> toBytes(final Stream<String> stream) {
    return StreamExtensions.toBytes(stream, "UTF-8");
  }
  
  public static Stream<List<Byte>> toBytes(final Stream<String> stream, final String encoding) {
    final Function1<String, List<Byte>> _function = new Function1<String, List<Byte>>() {
      public List<Byte> apply(final String it) {
        try {
          byte[] _bytes = (it + "\n").getBytes(encoding);
          return ((List<Byte>) Conversions.doWrapArray(_bytes));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return StreamExtensions.<String, List<Byte>>map(stream, _function);
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  @Async
  public static void writeTo(final Stream<List<Byte>> stream, final OutputStream out, final Task task) {
    final Procedure1<Stream<List<Byte>>> _function = new Procedure1<Stream<List<Byte>>>() {
      public void apply(final Stream<List<Byte>> it) {
        try {
          out.close();
          task.complete();
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    Subscription<List<Byte>> _onClosed = StreamExtensions.<List<Byte>>onClosed(stream, _function);
    final Procedure1<Finish<List<Byte>>> _function_1 = new Procedure1<Finish<List<Byte>>>() {
      public void apply(final Finish<List<Byte>> it) {
        try {
          if ((it.level == 0)) {
            out.close();
          }
          task.complete();
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    Subscription<List<Byte>> _onFinish = StreamExtensions.<List<Byte>>onFinish(_onClosed, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        task.error(it);
      }
    };
    Subscription<List<Byte>> _onError = StreamExtensions.<List<Byte>>onError(_onFinish, _function_2);
    final Procedure1<List<Byte>> _function_3 = new Procedure1<List<Byte>>() {
      public void apply(final List<Byte> it) {
        try {
          out.write(((byte[])Conversions.unwrapArray(it, byte.class)));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    StreamExtensions.<List<Byte>>onEach(_onError, _function_3);
  }
  
  /**
   * write a buffered bytestream to a file
   */
  @Async
  public static void writeTo(final Stream<List<Byte>> stream, final File file, final Task task) {
    try {
      final ByteSink sink = Files.asByteSink(file);
      final BufferedOutputStream out = sink.openBufferedStream();
      StreamExtensions.writeTo(stream, out, task);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * Complete a task when the stream finishes or closes
   */
  @Async
  public static Task toTask(final Stream<?> stream, final Task task) {
    final Procedure1<Stream<?>> _function = new Procedure1<Stream<?>>() {
      public void apply(final Stream<?> it) {
        task.complete();
      }
    };
    Subscription<?> _onClosed = StreamExtensions.onClosed(stream, _function);
    final Procedure1<Finish<?>> _function_1 = new Procedure1<Finish<?>>() {
      public void apply(final Finish<?> it) {
        task.complete();
      }
    };
    Subscription<?> _onFinish = StreamExtensions.onFinish(_onClosed, _function_1);
    final Procedure1<Object> _function_2 = new Procedure1<Object>() {
      public void apply(final Object it) {
      }
    };
    return StreamExtensions.onEach(_onFinish, _function_2);
  }
  
  /**
   * Peek into what values going through the stream chain at this point.
   * It is meant as a debugging tool for inspecting the data flowing
   * through the stream.
   * <p>
   * The listener will not modify the stream and only get a view of the
   * data passing by. It should never modify the passed reference!
   * <p>
   * If the listener throws an error, it will be caught and printed,
   * and not interrupt the stream or throw an error on the stream.
   */
  public static <T extends Object> Stream<T> peek(final Stream<T> stream, final Procedure1<? super T> listener) {
    final Function1<T, T> _function = new Function1<T, T>() {
      public T apply(final T it) {
        T _xblockexpression = null;
        {
          try {
            listener.apply(it);
          } catch (final Throwable _t) {
            if (_t instanceof Throwable) {
              final Throwable t = (Throwable)_t;
              t.printStackTrace();
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          _xblockexpression = it;
        }
        return _xblockexpression;
      }
    };
    return StreamExtensions.<T, T>map(stream, _function);
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  public static Task writeTo(final Stream<List<Byte>> stream, final OutputStream out) {
    final Task task = new Task();
    try {
    	writeTo(stream,out,task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  public static Task writeTo(final Executor executor, final Stream<List<Byte>> stream, final OutputStream out) {
    final Task task = new Task();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			writeTo(stream,out,task);
    		} catch(Throwable t) {
    			task.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return task;
  }
  
  /**
   * write a buffered bytestream to a file
   */
  public static Task writeTo(final Stream<List<Byte>> stream, final File file) {
    final Task task = new Task();
    try {
    	writeTo(stream,file,task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  /**
   * write a buffered bytestream to a file
   */
  public static Task writeTo(final Executor executor, final Stream<List<Byte>> stream, final File file) {
    final Task task = new Task();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			writeTo(stream,file,task);
    		} catch(Throwable t) {
    			task.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return task;
  }
  
  /**
   * Complete a task when the stream finishes or closes
   */
  public static Task toTask(final Stream<?> stream) {
    final Task task = new Task();
    try {
    	toTask(stream,task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  /**
   * Complete a task when the stream finishes or closes
   */
  public static Task toTask(final Executor executor, final Stream<?> stream) {
    final Task task = new Task();
    final Runnable toRun = new Runnable() {
    	public void run() {
    		try {
    			toTask(stream,task);
    		} catch(Throwable t) {
    			task.error(t);
    		}
    	}
    };
    executor.execute(toRun);
    return task;
  }
}
