package nl.kii.stream;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Async;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamException;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.StreamResponder;
import nl.kii.stream.StreamResponderBuilder;
import nl.kii.stream.Value;
import nl.kii.stream.source.LoadBalancer;
import nl.kii.stream.source.StreamCopySplitter;
import nl.kii.stream.source.StreamSource;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.DoubleExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class StreamExtensions {
  /**
   * Create a stream of the given type
   */
  public static <T extends Object> Stream<T> stream(final Class<T> type) {
    return new Stream<T>();
  }
  
  /**
   * Create a stream of a set of data and finish it.
   * Note: the reason this method is called datastream instead of stream, is that
   * the type binds to anything, even void. That means that datastream() becomes a valid expression
   * which is errorprone.
   */
  public static <T extends Object> Stream<T> datastream(final T... data) {
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
   * Create a stream of values out of a Promise of a list. If the promise throws an error,
   */
  public static <T extends Object, T2 extends Iterable<T>> Stream<T> stream(final IPromise<T2> promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          newStream.error(it);
        }
      };
      IPromise<T2> _onError = promise.onError(_function);
      final Procedure1<T2> _function_1 = new Procedure1<T2>() {
        public void apply(final T2 it) {
          Stream<T> _stream = StreamExtensions.<T>stream(it);
          StreamExtensions.<T>pipe(_stream, newStream);
        }
      };
      _onError.then(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * stream an list, ending with a finish. makes an immutable copy internally.
   */
  public static <T extends Object> Stream<T> stream(final List<T> list) {
    ImmutableList<T> _copyOf = ImmutableList.<T>copyOf(list);
    UnmodifiableIterator<T> _iterator = _copyOf.iterator();
    return StreamExtensions.<T>stream(_iterator);
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
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              pushNext.apply();
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              finished.set(true);
              stream.finish();
            }
          };
          it.skip(_function_1);
        }
      };
      StreamExtensions.<T>monitor(stream, _function_1);
      stream.setOperation("iterate");
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
              Boolean _isOpen = newStream.isOpen();
              boolean _not = (!(_isOpen).booleanValue());
              if (_not) {
                return false;
              }
              newStream.push(((List<Byte>)Conversions.doWrapArray(buf)));
              _xblockexpression = true;
            }
            return _xblockexpression;
          }
        });
        final Procedure1<StreamResponder> _function = new Procedure1<StreamResponder>() {
          public void apply(final StreamResponder it) {
            final Procedure1<Void> _function = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.skip(_function);
            final Procedure1<Void> _function_1 = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.close(_function_1);
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
   * create an unending stream of random integers in the range you have given
   */
  public static Stream<Integer> streamRandom(final IntegerRange range) {
    Stream<Integer> _xblockexpression = null;
    {
      final Random randomizer = new Random();
      final Stream<Integer> newStream = StreamExtensions.<Integer>stream(int.class);
      final Procedure1<StreamResponder> _function = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              Boolean _isOpen = newStream.isOpen();
              if ((_isOpen).booleanValue()) {
                int _start = range.getStart();
                int _size = range.getSize();
                int _nextInt = randomizer.nextInt(_size);
                final int next = (_start + _nextInt);
                newStream.push(Integer.valueOf(next));
              }
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.close(_function_2);
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
      stream.setOperation("publish");
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
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              stopObserving.apply();
            }
          };
          it.close(_function);
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
   * pipe a stream into another stream
   */
  public static <T extends Object> StreamSource<T> operator_doubleGreaterThan(final Stream<T> source, final Stream<T> dest) {
    return StreamExtensions.<T>pipe(source, dest);
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <T extends Object> StreamSource<T> operator_doubleLessThan(final Stream<T> dest, final Stream<T> source) {
    return StreamExtensions.<T>pipe(source, dest);
  }
  
  /**
   * split a source into a new destination stream
   */
  public static <T extends Object> StreamSource<T> operator_doubleGreaterThan(final StreamSource<T> source, final Stream<T> dest) {
    return source.pipe(dest);
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
  
  public static <T extends Object, R extends Object> void controls(final Stream<T> newStream, final Stream<?> parent) {
    final Procedure1<StreamResponder> _function = new Procedure1<StreamResponder>() {
      public void apply(final StreamResponder it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.next();
          }
        };
        it.next(_function);
        final Procedure1<Void> _function_1 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.skip();
          }
        };
        it.skip(_function_1);
        final Procedure1<Void> _function_2 = new Procedure1<Void>() {
          public void apply(final Void it) {
            parent.close();
          }
        };
        it.close(_function_2);
      }
    };
    StreamExtensions.<T>monitor(newStream, _function);
  }
  
  /**
   * Tell the stream something went wrong
   */
  public static <T extends Object> void error(final Stream<T> stream, final String message) {
    Exception _exception = new Exception(message);
    stream.error(_exception);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <T extends Object> void error(final Stream<T> stream, final String message, final Throwable cause) {
    Exception _exception = new Exception(message, cause);
    stream.error(_exception);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <T extends Object> void error(final Stream<T> stream, final String message, final T value, final Throwable cause) {
    StreamException _streamException = new StreamException(message, value, cause);
    stream.error(_streamException);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <T extends Object> void error(final Stream<T> stream, final String message, final T value) {
    StreamException _streamException = new StreamException(message, value, null);
    stream.error(_streamException);
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn
   */
  public static <T extends Object, R extends Object> Stream<R> map(final Stream<T> stream, final Function1<? super T, ? extends R> mappingFn) {
    Stream<R> _xblockexpression = null;
    {
      final Stream<R> newStream = new Stream<R>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              final R mapped = mappingFn.apply(it);
              newStream.push(mapped);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<R, Object>controls(newStream, stream);
      stream.setOperation("map");
      _xblockexpression = newStream;
    }
    return _xblockexpression;
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              index.set(0);
              passed.set(0);
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, stream);
      stream.setOperation("filter");
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              boolean _get = justPostedFinish0.get();
              if (_get) {
                Finish<Object> _finish = new Finish<Object>(((it).intValue() + 1));
                newStream.apply(_finish);
              } else {
                justPostedFinish0.set(true);
                Finish<Object> _finish_1 = new Finish<Object>(0);
                Finish<Object> _finish_2 = new Finish<Object>(((it).intValue() + 1));
                final List<Finish<Object>> entries = Collections.<Finish<Object>>unmodifiableList(CollectionLiterals.<Finish<Object>>newArrayList(_finish_1, _finish_2));
                Entries<Object> _entries = new Entries<Object>(((Entry<Object>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              }
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("split");
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Merges one level of finishes.
   * @see StreamExtensions.split
   */
  public static <T extends Object> Stream<T> merge(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Value<T> _value = new Value<T>(it);
              newStream.apply(_value);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() > 0)) {
                newStream.finish(((it).intValue() - 1));
              } else {
                stream.next();
              }
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("merge");
      StreamExtensions.<T, Object>controls(newStream, stream);
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
    Stream<T> _until = StreamExtensions.<T>until(stream, _function);
    final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_until, _function_1);
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              index.set(0);
              passed.set(0);
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<T, Object>controls(newStream, stream);
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
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              count.set(0);
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Flatten a stream of streams into a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <T extends Object> Stream<T> flatten(final Stream<Stream<T>> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<Stream<T>>> _function = new Procedure1<StreamHandlerBuilder<Stream<T>>>() {
        public void apply(final StreamHandlerBuilder<Stream<T>> it) {
          final Procedure1<Stream<T>> _function = new Procedure1<Stream<T>>() {
            public void apply(final Stream<T> s) {
              final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
                public void apply(final StreamHandlerBuilder<T> it) {
                  final Procedure1<T> _function = new Procedure1<T>() {
                    public void apply(final T it) {
                      newStream.push(it);
                      s.next();
                    }
                  };
                  it.each(_function);
                  final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
                    public Boolean apply(final Throwable it) {
                      boolean _xblockexpression = false;
                      {
                        newStream.error(it);
                        s.next();
                        _xblockexpression = false;
                      }
                      return Boolean.valueOf(_xblockexpression);
                    }
                  };
                  it.error(_function_1);
                }
              };
              StreamExtensions.<T>on(s, _function);
              s.next();
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_2);
        }
      };
      StreamExtensions.<Stream<T>>on(stream, _function);
      stream.setOperation("flatten");
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Performs a flatmap operation on the stream using the passed mapping function.
   * <p>
   * Flatmapping allows you to transform the values of the stream to multiple streams,
   * which are then merged to a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <T extends Object, R extends Object> Stream<R> flatMap(final Stream<T> stream, final Function1<? super T, ? extends Stream<R>> mapFn) {
    Stream<Stream<R>> _map = StreamExtensions.<T, Stream<R>>map(stream, mapFn);
    Stream<R> _flatten = StreamExtensions.<R>flatten(_map);
    final Procedure1<Stream<R>> _function = new Procedure1<Stream<R>>() {
      public void apply(final Stream<R> it) {
        stream.setOperation("flatmap");
      }
    };
    return ObjectExtensions.<Stream<R>>operator_doubleArrow(_flatten, _function);
  }
  
  /**
   * Keep count of how many items have been streamed so far, and for each
   * value from the original stream, push a pair of count->value.
   * Finish(0) resets the count.
   */
  public static <T extends Object> Stream<Pair<Integer, T>> index(final Stream<T> stream) {
    Stream<Pair<Integer, T>> _xblockexpression = null;
    {
      final Stream<Pair<Integer, T>> newStream = new Stream<Pair<Integer, T>>();
      final AtomicInteger counter = new AtomicInteger(0);
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              int _incrementAndGet = counter.incrementAndGet();
              Pair<Integer, T> _mappedTo = Pair.<Integer, T>of(Integer.valueOf(_incrementAndGet), it);
              newStream.push(_mappedTo);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() == 0)) {
                counter.set(0);
              }
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("index");
      StreamExtensions.<Pair<Integer, T>, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a splitter StreamSource from a stream that lets you listen to the stream
   * with multiple listeners.
   */
  public static <T extends Object> StreamCopySplitter<T> split(final Stream<T> stream) {
    return new StreamCopySplitter<T>(stream);
  }
  
  /**
   * Balance the stream into multiple listening streams.
   */
  public static <T extends Object> LoadBalancer<T> balance(final Stream<T> stream) {
    return new LoadBalancer<T>(stream);
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds to pass through the stream.
   * All other values are dropped.
   */
  public static <T extends Object> Stream<T> throttle(final Stream<T> stream, final int periodMs) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicLong startTime = new AtomicLong((-1));
      final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
        public Boolean apply(final T it) {
          boolean _xblockexpression = false;
          {
            final long now = System.currentTimeMillis();
            boolean _xifexpression = false;
            boolean _or = false;
            long _get = startTime.get();
            boolean _equals = (_get == (-1));
            if (_equals) {
              _or = true;
            } else {
              long _get_1 = startTime.get();
              long _minus = (now - _get_1);
              boolean _greaterThan = (_minus > periodMs);
              _or = _greaterThan;
            }
            if (_or) {
              boolean _xblockexpression_1 = false;
              {
                startTime.set(now);
                _xblockexpression_1 = true;
              }
              _xifexpression = _xblockexpression_1;
            } else {
              _xifexpression = false;
            }
            _xblockexpression = _xifexpression;
          }
          return Boolean.valueOf(_xblockexpression);
        }
      };
      Stream<T> _filter = StreamExtensions.<T>filter(stream, _function);
      final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
        public void apply(final Stream<T> it) {
          stream.setOperation((("throttle(periodMs=" + Integer.valueOf(periodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<Stream<T>>operator_doubleArrow(_filter, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds to pass through the stream.
   * All other values are buffered, and dropped only after the buffer has reached a given size.
   */
  @Deprecated
  public static <T extends Object> Stream<T> ratelimit(final Stream<T> stream, final int periodMs, final int bufferSize) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicLong startTime = new AtomicLong((-1));
      final Function1<T, Boolean> _function = new Function1<T, Boolean>() {
        public Boolean apply(final T it) {
          boolean _xblockexpression = false;
          {
            final long now = System.currentTimeMillis();
            boolean _xifexpression = false;
            boolean _or = false;
            long _get = startTime.get();
            boolean _equals = (_get == (-1));
            if (_equals) {
              _or = true;
            } else {
              long _get_1 = startTime.get();
              long _minus = (now - _get_1);
              boolean _greaterThan = (_minus > periodMs);
              _or = _greaterThan;
            }
            if (_or) {
              boolean _xblockexpression_1 = false;
              {
                startTime.set(now);
                _xblockexpression_1 = true;
              }
              _xifexpression = _xblockexpression_1;
            } else {
              _xifexpression = false;
            }
            _xblockexpression = _xifexpression;
          }
          return Boolean.valueOf(_xblockexpression);
        }
      };
      Stream<T> _filter = StreamExtensions.<T>filter(stream, _function);
      final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
        public void apply(final Stream<T> it) {
          stream.setOperation((((("ratelimit(periodMs=" + Integer.valueOf(periodMs)) + ",bufferSize=") + Integer.valueOf(bufferSize)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<Stream<T>>operator_doubleArrow(_filter, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
   * <p>
   * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
   */
  public static <T extends Object> Stream<T> synchronizeWith(final Stream<T> stream, final Stream<?> timerStream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<?>> _function = new Procedure1<StreamHandlerBuilder<?>>() {
        public void apply(final StreamHandlerBuilder<?> it) {
          final Function1<Throwable, Boolean> _function = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                stream.error(it);
                timerStream.next();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish();
              timerStream.next();
            }
          };
          it.finish(_function_1);
          final Procedure1<Object> _function_2 = new Procedure1<Object>() {
            public void apply(final Object it) {
              Boolean _isOpen = stream.isOpen();
              if ((_isOpen).booleanValue()) {
                stream.next();
              } else {
                timerStream.close();
              }
              timerStream.next();
            }
          };
          it.each(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.on(timerStream, _function);
      final Procedure1<StreamHandlerBuilder<T>> _function_1 = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              newStream.push(it);
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function_1);
      final Procedure1<StreamResponder> _function_2 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_1);
        }
      };
      StreamExtensions.<T>monitor(newStream, _function_2);
      stream.setOperation("forEvery");
      timerStream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Always get the latest value that was on the stream.
   * FIX: does not work yet. Will require streams to allow multiple listeners
   */
  @Deprecated
  public static <T extends Object> Stream<T> latest(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> latest = new AtomicReference<T>();
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              latest.set(it);
              stream.next();
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                stream.next();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
              stream.next();
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              T _get = latest.get();
              newStream.push(_get);
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_1);
        }
      };
      StreamExtensions.<T>monitor(newStream, _function_1);
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
    Stream<T> _resolve = StreamExtensions.<T, Object>resolve(stream, 1);
    final Procedure1<Stream<T>> _function = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        stream.setOperation("resolve");
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * <p>
   * Allows concurrent promises to be resolved in parallel.
   */
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<? extends IPromise<T>> stream, final int concurrency) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final AtomicBoolean isFinished = new AtomicBoolean(false);
      final AtomicInteger processes = new AtomicInteger(0);
      final Procedure1<StreamHandlerBuilder<? extends IPromise<T>>> _function = new Procedure1<StreamHandlerBuilder<? extends IPromise<T>>>() {
        public void apply(final StreamHandlerBuilder<? extends IPromise<T>> it) {
          final Procedure1<IPromise<T>> _function = new Procedure1<IPromise<T>>() {
            public void apply(final IPromise<T> promise) {
              processes.incrementAndGet();
              final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
                public void apply(final Throwable it) {
                  processes.decrementAndGet();
                  StreamException _streamException = new StreamException("resolve", null, it);
                  newStream.error(_streamException);
                  boolean _get = isFinished.get();
                  if (_get) {
                    newStream.finish();
                  }
                }
              };
              IPromise<T> _onError = promise.onError(_function);
              final Procedure1<T> _function_1 = new Procedure1<T>() {
                public void apply(final T it) {
                  processes.decrementAndGet();
                  newStream.push(it);
                  boolean _get = isFinished.get();
                  if (_get) {
                    newStream.finish();
                  }
                }
              };
              _onError.then(_function_1);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              int _get = processes.get();
              boolean _equals = (_get == 0);
              if (_equals) {
                newStream.finish((it).intValue());
              } else {
                isFinished.set(true);
              }
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.on(stream, _function);
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              int _get = processes.get();
              boolean _greaterThan = (concurrency > _get);
              if (_greaterThan) {
                stream.next();
              }
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_2);
        }
      };
      StreamExtensions.<T>monitor(newStream, _function_1);
      stream.setOperation((("resolve(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(1)
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<T> stream, final Function1<? super T, ? extends P> promiseFn) {
    Stream<R> _call = StreamExtensions.<T, R, P>call(stream, 1, promiseFn);
    final Procedure1<Stream<R>> _function = new Procedure1<Stream<R>>() {
      public void apply(final Stream<R> it) {
        stream.setOperation("call");
      }
    };
    return ObjectExtensions.<Stream<R>>operator_doubleArrow(_call, _function);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> Stream<R> call(final Stream<T> stream, final int concurrency, final Function1<? super T, ? extends P> promiseFn) {
    Stream<P> _map = StreamExtensions.<T, P>map(stream, promiseFn);
    Stream<R> _resolve = StreamExtensions.<R, Object>resolve(_map, concurrency);
    final Procedure1<Stream<R>> _function = new Procedure1<Stream<R>>() {
      public void apply(final Stream<R> it) {
        stream.setOperation((("call(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      }
    };
    return ObjectExtensions.<Stream<R>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Handle errors on the stream.  This will swallow the error from the stream.
   * @return a new stream like the incoming stream but without the caught errors.
   */
  public static <T extends Object> Stream<T> onError(final Stream<T> stream, final Procedure1<? super Throwable> handler) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              newStream.push(it);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                handler.apply(it);
                stream.next();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Stream<T> onClosed(final Stream<T> stream, final Procedure1<? super Void> handler) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              newStream.push(it);
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              try {
                handler.apply(null);
              } finally {
                newStream.close();
              }
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <T extends Object> Task onEach(final Stream<T> stream, final Procedure1<? super T> handler) {
    final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
      public void apply(final StreamHandlerBuilder<T> it) {
        final Procedure1<T> _function = new Procedure1<T>() {
          public void apply(final T it) {
            handler.apply(it);
            stream.next();
          }
        };
        it.each(_function);
        final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
          public Boolean apply(final Throwable it) {
            boolean _xblockexpression = false;
            {
              stream.next();
              _xblockexpression = true;
            }
            return Boolean.valueOf(_xblockexpression);
          }
        };
        it.error(_function_1);
        final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            stream.next();
          }
        };
        it.finish(_function_2);
        final Procedure1<Void> _function_3 = new Procedure1<Void>() {
          public void apply(final Void it) {
            stream.close();
          }
        };
        it.closed(_function_3);
      }
    };
    Task _on = StreamExtensions.<T>on(stream, _function);
    final Procedure1<Task> _function_1 = new Procedure1<Task>() {
      public void apply(final Task it) {
        stream.setOperation("onEach");
        stream.next();
      }
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_on, _function_1);
  }
  
  /**
   * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Performs the task for every value, and only requests the next value from the stream once the task has finished.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> Task onEachCall(final Stream<T> stream, final Function1<? super T, ? extends P> taskFn) {
    Stream<P> _map = StreamExtensions.<T, P>map(stream, taskFn);
    Stream<R> _resolve = StreamExtensions.<R, Object>resolve(_map);
    final Procedure1<R> _function = new Procedure1<R>() {
      public void apply(final R it) {
      }
    };
    Task _onEach = StreamExtensions.<R>onEach(_resolve, _function);
    final Procedure1<Task> _function_1 = new Procedure1<Task>() {
      public void apply(final Task it) {
        stream.setOperation("onEachCall");
      }
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_onEach, _function_1);
  }
  
  /**
   * Shortcut for splitting a stream and then performing a pipe to another stream.
   * @return a CopySplitter source that you can connect more streams to.
   */
  public static <T extends Object> StreamSource<T> pipe(final Stream<T> stream, final Stream<T> target) {
    StreamCopySplitter<T> _split = StreamExtensions.<T>split(stream);
    StreamSource<T> _pipe = _split.pipe(target);
    final Procedure1<StreamSource<T>> _function = new Procedure1<StreamSource<T>>() {
      public void apply(final StreamSource<T> it) {
        stream.setOperation("pipe");
      }
    };
    return ObjectExtensions.<StreamSource<T>>operator_doubleArrow(_pipe, _function);
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Closes the stream once it has the value or an error.
   */
  public static <T extends Object> IPromise<T> first(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.set(it);
              }
              stream.close();
            }
          };
          it.each(_function);
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                Boolean _fulfilled = promise.getFulfilled();
                boolean _not = (!(_fulfilled).booleanValue());
                if (_not) {
                  promise.error(it);
                }
                stream.close();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              PromiseExtensions.<T>error(promise, "Stream.first: stream finished without returning a value");
              stream.close();
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              PromiseExtensions.<T>error(promise, "Stream.first: stream closed without returning a value");
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("first");
      stream.next();
      _xblockexpression = promise;
    }
    return _xblockexpression;
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Will keep asking next on the stream until it gets to the last value!
   * Skips any stream errors, and closes the stream when it is done.
   */
  public static <T extends Object> IPromise<T> last(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final AtomicReference<T> last = new AtomicReference<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                last.set(it);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() == 0)) {
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
                  stream.close();
                } else {
                  PromiseExtensions.<T>error(promise, "stream finished without passing a value, no last entry found.");
                }
              } else {
                stream.next();
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
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
                stream.close();
              } else {
                PromiseExtensions.<T>error(promise, "stream closed without passing a value, no last entry found.");
              }
            }
          };
          it.closed(_function_2);
          final Function1<Throwable, Boolean> _function_3 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                stream.next();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("last");
      stream.next();
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
    Stream<T> _filter = StreamExtensions.<T>filter(stream, _function);
    final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        stream.setOperation((("skip(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_filter, _function_1);
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
    Stream<T> _until = StreamExtensions.<T>until(stream, _function);
    final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_until, _function_1);
  }
  
  /**
   * Start the stream and and promise the first value from it.
   */
  public static <T extends Object> Task then(final Stream<T> stream, final Procedure1<T> listener) {
    IPromise<T> _first = StreamExtensions.<T>first(stream);
    Task _then = _first.then(listener);
    final Procedure1<Task> _function = new Procedure1<Task>() {
      public void apply(final Task it) {
        stream.setOperation("then");
      }
    };
    return ObjectExtensions.<Task>operator_doubleArrow(_then, _function);
  }
  
  /**
   * Convenient builder to easily asynchronously respond to stream entries.
   * Defaults for each[], finish[], and error[] is to simply ask for the next entry.
   * <p>
   * Note: you can only have a single entry handler for a stream.
   * <p>
   * Usage example:
   * <pre>
   * val stream = (1.10).stream
   * stream.on [
   *    each [ println('got ' + it) stream.next ] // do not forget to ask for the next entry
   *    error [ printStackTrace stream.next true ] // true to indicate you want to throw the error
   * ]
   * stream.next // do not forget to start the stream!
   * </pre>
   * @return a task that completes on finish(0) or closed, or that gives an error
   * if the stream passed an error.
   */
  public static <T extends Object> Task on(final Stream<T> stream, final Procedure1<? super StreamHandlerBuilder<T>> handlerFn) {
    final Procedure2<Stream<T>, StreamHandlerBuilder<T>> _function = new Procedure2<Stream<T>, StreamHandlerBuilder<T>>() {
      public void apply(final Stream<T> s, final StreamHandlerBuilder<T> builder) {
        handlerFn.apply(builder);
      }
    };
    return StreamExtensions.<T>on(stream, _function);
  }
  
  public static <T extends Object> Task on(final Stream<T> stream, final Procedure2<? super Stream<T>, ? super StreamHandlerBuilder<T>> handlerFn) {
    Task _xblockexpression = null;
    {
      StreamHandlerBuilder<T> _streamHandlerBuilder = new StreamHandlerBuilder<T>(stream);
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              stream.next();
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                stream.next();
                _xblockexpression = true;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
        }
      };
      final StreamHandlerBuilder<T> handler = ObjectExtensions.<StreamHandlerBuilder<T>>operator_doubleArrow(_streamHandlerBuilder, _function);
      final Task task = stream.observe(handler);
      handlerFn.apply(stream, handler);
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Convenient builder to easily respond to commands given to a stream.
   * <p>
   * Note: you can only have a single monitor for a stream.
   * <p>
   * Example:
   * <pre>
   * stream.monitor [
   *     next [ println('next was called on the stream') ]
   *     close [ println('the stream was closed') ]
   * ]
   * </pre>
   */
  public static <T extends Object> void monitor(final Stream<T> stream, final Procedure1<? super StreamResponder> handlerFn) {
    final StreamResponderBuilder handler = new StreamResponderBuilder();
    handlerFn.apply(handler);
    stream.monitor(handler);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <T extends Object> Stream<T> effect(final Stream<T> stream, final Procedure1<? super T> listener) {
    final Function1<T, T> _function = new Function1<T, T>() {
      public T apply(final T it) {
        T _xblockexpression = null;
        {
          listener.apply(it);
          _xblockexpression = it;
        }
        return _xblockexpression;
      }
    };
    Stream<T> _map = StreamExtensions.<T, T>map(stream, _function);
    final Procedure1<Stream<T>> _function_1 = new Procedure1<Stream<T>>() {
      public void apply(final Stream<T> it) {
        stream.setOperation("effect");
      }
    };
    return ObjectExtensions.<Stream<T>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * Opposite of collect, separate each list in the stream into separate
   * stream entries and streams those separately.
   */
  public static <T extends Object> Stream<T> separate(final Stream<List<T>> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<List<T>>> _function = new Procedure1<StreamHandlerBuilder<List<T>>>() {
        public void apply(final StreamHandlerBuilder<List<T>> it) {
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
          final Function1<Throwable, Boolean> _function_1 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_1);
          final Procedure1<Integer> _function_2 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<List<T>>on(stream, _function);
      stream.setOperation("separate");
      StreamExtensions.<T, Object>controls(newStream, stream);
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
      ArrayList<T> _newArrayList = CollectionLiterals.<T>newArrayList();
      final Function2<List<T>, T, List<T>> _function = new Function2<List<T>, T, List<T>>() {
        public List<T> apply(final List<T> list, final T it) {
          return StreamExtensions.<T>concat(list, it);
        }
      };
      final Stream<List<T>> s = StreamExtensions.<T, List<T>>reduce(stream, _newArrayList, _function);
      stream.setOperation("collect");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Concatenate a lot of strings into a single string, separated by a separator string.
   * <pre>
   * (1..3).stream.join('-').then [ println(it) ] // prints 1-2-3
   */
  public static <T extends Object> Stream<String> join(final Stream<T> stream, final String separator) {
    final Function2<String, T, String> _function = new Function2<String, T, String>() {
      public String apply(final String acc, final T it) {
        String _xifexpression = null;
        boolean _notEquals = (!Objects.equal(acc, ""));
        if (_notEquals) {
          _xifexpression = separator;
        } else {
          _xifexpression = "";
        }
        String _plus = (acc + _xifexpression);
        String _string = it.toString();
        return (_plus + _string);
      }
    };
    Stream<String> _reduce = StreamExtensions.<T, String>reduce(stream, "", _function);
    final Procedure1<Stream<String>> _function_1 = new Procedure1<Stream<String>>() {
      public void apply(final Stream<String> it) {
        stream.setOperation("join");
      }
    };
    return ObjectExtensions.<Stream<String>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Add the value of all the items in the stream until a finish.
   */
  public static <T extends Number> Stream<Double> sum(final Stream<T> stream) {
    final Function2<Double, T, Double> _function = new Function2<Double, T, Double>() {
      public Double apply(final Double acc, final T it) {
        double _doubleValue = it.doubleValue();
        return Double.valueOf(((acc).doubleValue() + _doubleValue));
      }
    };
    Stream<Double> _reduce = StreamExtensions.<T, Double>reduce(stream, Double.valueOf(0D), _function);
    final Procedure1<Stream<Double>> _function_1 = new Procedure1<Stream<Double>>() {
      public void apply(final Stream<Double> it) {
        stream.setOperation("sum");
      }
    };
    return ObjectExtensions.<Stream<Double>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Average the items in the stream until a finish.
   */
  public static <T extends Number> Stream<Double> average(final Stream<T> stream) {
    Stream<Pair<Integer, T>> _index = StreamExtensions.<T>index(stream);
    Pair<Integer, Double> _mappedTo = Pair.<Integer, Double>of(Integer.valueOf(0), Double.valueOf(0D));
    final Function2<Pair<Integer, Double>, Pair<Integer, T>, Pair<Integer, Double>> _function = new Function2<Pair<Integer, Double>, Pair<Integer, T>, Pair<Integer, Double>>() {
      public Pair<Integer, Double> apply(final Pair<Integer, Double> acc, final Pair<Integer, T> it) {
        Integer _key = it.getKey();
        Double _value = acc.getValue();
        T _value_1 = it.getValue();
        double _doubleValue = _value_1.doubleValue();
        double _plus = ((_value).doubleValue() + _doubleValue);
        return Pair.<Integer, Double>of(_key, Double.valueOf(_plus));
      }
    };
    Stream<Pair<Integer, Double>> _reduce = StreamExtensions.<Pair<Integer, T>, Pair<Integer, Double>>reduce(_index, _mappedTo, _function);
    final Function1<Pair<Integer, Double>, Double> _function_1 = new Function1<Pair<Integer, Double>, Double>() {
      public Double apply(final Pair<Integer, Double> it) {
        Double _value = it.getValue();
        Integer _key = it.getKey();
        return Double.valueOf(DoubleExtensions.operator_divide(_value, _key));
      }
    };
    Stream<Double> _map = StreamExtensions.<Pair<Integer, Double>, Double>map(_reduce, _function_1);
    final Procedure1<Stream<Double>> _function_2 = new Procedure1<Stream<Double>>() {
      public void apply(final Stream<Double> it) {
        stream.setOperation("average");
      }
    };
    return ObjectExtensions.<Stream<Double>>operator_doubleArrow(_map, _function_2);
  }
  
  /**
   * Count the number of items passed in the stream until a finish.
   */
  public static <T extends Object> Stream<Integer> count(final Stream<T> stream) {
    final Function2<Integer, T, Integer> _function = new Function2<Integer, T, Integer>() {
      public Integer apply(final Integer acc, final T it) {
        return Integer.valueOf(((acc).intValue() + 1));
      }
    };
    Stream<Integer> _reduce = StreamExtensions.<T, Integer>reduce(stream, Integer.valueOf(0), _function);
    final Procedure1<Stream<Integer>> _function_1 = new Procedure1<Stream<Integer>>() {
      public void apply(final Stream<Integer> it) {
        stream.setOperation("count");
      }
    };
    return ObjectExtensions.<Stream<Integer>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Gives the maximum value found on the stream.
   * Values must implement Comparable
   */
  public static <T extends Comparable<T>> Stream<T> max(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Function2<T, T, T> _function = new Function2<T, T, T>() {
        public T apply(final T acc, final T it) {
          T _xifexpression = null;
          boolean _and = false;
          boolean _notEquals = (!Objects.equal(acc, null));
          if (!_notEquals) {
            _and = false;
          } else {
            int _compareTo = acc.compareTo(it);
            boolean _greaterThan = (_compareTo > 0);
            _and = _greaterThan;
          }
          if (_and) {
            _xifexpression = acc;
          } else {
            _xifexpression = it;
          }
          return _xifexpression;
        }
      };
      final Stream<T> s = StreamExtensions.<T, T>reduce(stream, null, _function);
      stream.setOperation("max");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Gives the minimum value found on the stream.
   * Values must implement Comparable
   */
  public static <T extends Comparable<T>> Stream<T> min(final Stream<T> stream) {
    Stream<T> _xblockexpression = null;
    {
      final Function2<T, T, T> _function = new Function2<T, T, T>() {
        public T apply(final T acc, final T it) {
          T _xifexpression = null;
          boolean _and = false;
          boolean _notEquals = (!Objects.equal(acc, null));
          if (!_notEquals) {
            _and = false;
          } else {
            int _compareTo = acc.compareTo(it);
            boolean _lessThan = (_compareTo < 0);
            _and = _lessThan;
          }
          if (_and) {
            _xifexpression = acc;
          } else {
            _xifexpression = it;
          }
          return _xifexpression;
        }
      };
      final Stream<T> s = StreamExtensions.<T, T>reduce(stream, null, _function);
      stream.setOperation("min");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * Errors in the stream are suppressed.
   */
  public static <T extends Object, R extends Object> Stream<R> reduce(final Stream<T> stream, final R initial, final Function2<? super R, ? super T, ? extends R> reducerFn) {
    Stream<R> _xblockexpression = null;
    {
      final AtomicReference<R> reduced = new AtomicReference<R>(initial);
      final Stream<R> newStream = new Stream<R>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              R _get = reduced.get();
              R _apply = reducerFn.apply(_get, it);
              reduced.set(_apply);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() == 0)) {
                final R result = reduced.getAndSet(initial);
                boolean _notEquals = (!Objects.equal(result, null));
                if (_notEquals) {
                  newStream.push(result);
                } else {
                  StreamExtensions.<R>error(newStream, "no result found when reducing");
                }
              } else {
                newStream.finish(((it).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                stream.next();
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation((("reduce(initial=" + initial) + ")"));
      StreamExtensions.<R, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object, R extends Object> Stream<R> scan(final Stream<T> stream, final R initial, final Function2<? super R, ? super T, ? extends R> reducerFn) {
    Stream<R> _xblockexpression = null;
    {
      final AtomicReference<R> reduced = new AtomicReference<R>(initial);
      final Stream<R> newStream = new Stream<R>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              R _get = reduced.get();
              final R result = reducerFn.apply(_get, it);
              reduced.set(result);
              boolean _notEquals = (!Objects.equal(result, null));
              if (_notEquals) {
                newStream.push(result);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              reduced.set(initial);
              newStream.finish((it).intValue());
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation((("scan(initial=" + initial) + ")"));
      StreamExtensions.<R, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if all stream values match the test function
   */
  public static <T extends Object> Stream<Boolean> all(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final Function2<Boolean, T, Boolean> _function = new Function2<Boolean, T, Boolean>() {
        public Boolean apply(final Boolean acc, final T it) {
          boolean _and = false;
          if (!(acc).booleanValue()) {
            _and = false;
          } else {
            Boolean _apply = testFn.apply(it);
            _and = (_apply).booleanValue();
          }
          return Boolean.valueOf(_and);
        }
      };
      final Stream<Boolean> s = StreamExtensions.<T, Boolean>reduce(stream, Boolean.valueOf(true), _function);
      stream.setOperation("all");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if no stream values match the test function
   */
  public static <T extends Object> Stream<Boolean> none(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final Function2<Boolean, T, Boolean> _function = new Function2<Boolean, T, Boolean>() {
        public Boolean apply(final Boolean acc, final T it) {
          boolean _and = false;
          if (!(acc).booleanValue()) {
            _and = false;
          } else {
            Boolean _apply = testFn.apply(it);
            boolean _not = (!(_apply).booleanValue());
            _and = _not;
          }
          return Boolean.valueOf(_and);
        }
      };
      final Stream<Boolean> s = StreamExtensions.<T, Boolean>reduce(stream, Boolean.valueOf(true), _function);
      stream.setOperation("none");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if any of the values match the passed testFn.
   * <p>
   * Note that this is not a normal reduction, since no finish is needed
   * for any to fire true. The moment testFn gives off true, true is streamed
   * and the rest of the incoming values are skipped.
   */
  public static <T extends Object> Stream<Boolean> any(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final Stream<Boolean> newStream = new Stream<Boolean>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
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
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() == 0)) {
                final boolean matched = anyMatch.get();
                anyMatch.set(false);
                if ((!matched)) {
                  newStream.push(Boolean.valueOf(false));
                }
              } else {
                newStream.finish(((it).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("any");
      StreamExtensions.<Boolean, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams the first value that matches the testFn
   * <p>
   * Note that this is not a normal reduction, since no finish is needed to fire a value.
   * The moment testFn gives off true, the value is streamed and the rest of the incoming
   * values are skipped.
   */
  public static <T extends Object> Stream<T> first(final Stream<T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> match = new AtomicReference<T>();
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamHandlerBuilder<T>> _function = new Procedure1<StreamHandlerBuilder<T>>() {
        public void apply(final StreamHandlerBuilder<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = testFn.apply(it);
              if ((_apply).booleanValue()) {
                match.set(it);
                newStream.push(it);
                stream.skip();
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
            public void apply(final Integer it) {
              if (((it).intValue() == 0)) {
                match.set(null);
              } else {
                newStream.finish(((it).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
            public Boolean apply(final Throwable it) {
              boolean _xblockexpression = false;
              {
                newStream.error(it);
                _xblockexpression = false;
              }
              return Boolean.valueOf(_xblockexpression);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<T>on(stream, _function);
      stream.setOperation("first");
      StreamExtensions.<T, Object>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Complete a task when the stream finishes or closes,
   * or give an error on the task when the stream gives an error.
   */
  @Async
  public static void toTask(final Stream<?> stream, final Task task) {
    final Procedure1<StreamHandlerBuilder<?>> _function = new Procedure1<StreamHandlerBuilder<?>>() {
      public void apply(final StreamHandlerBuilder<?> it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          public void apply(final Void it) {
            task.complete();
          }
        };
        it.closed(_function);
        final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            stream.close();
            task.complete();
          }
        };
        it.finish(_function_1);
        final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
          public Boolean apply(final Throwable it) {
            boolean _xblockexpression = false;
            {
              stream.close();
              task.error(it);
              _xblockexpression = true;
            }
            return Boolean.valueOf(_xblockexpression);
          }
        };
        it.error(_function_2);
        final Procedure1<Object> _function_3 = new Procedure1<Object>() {
          public void apply(final Object it) {
          }
        };
        it.each(_function_3);
      }
    };
    StreamExtensions.on(stream, _function);
    stream.setOperation("toTask");
  }
  
  private static <T extends Object> List<T> concat(final Iterable<? extends T> list, final T value) {
    ImmutableList<T> _xblockexpression = null;
    {
      boolean _notEquals = (!Objects.equal(value, null));
      if (_notEquals) {
        ImmutableList.Builder<Object> _builder = ImmutableList.<Object>builder();
        _builder.add();
      }
      ImmutableList<T> _xifexpression = null;
      boolean _notEquals_1 = (!Objects.equal(value, null));
      if (_notEquals_1) {
        ImmutableList.Builder<T> _builder_1 = ImmutableList.<T>builder();
        ImmutableList.Builder<T> _addAll = _builder_1.addAll(list);
        ImmutableList.Builder<T> _add = _addAll.add(value);
        _xifexpression = _add.build();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  /**
   * Complete a task when the stream finishes or closes,
   * or give an error on the task when the stream gives an error.
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
}
