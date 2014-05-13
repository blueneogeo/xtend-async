package nl.kii.stream;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.Task;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

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
  
  public static <K extends Object, V extends Object> Stream<Map<K,V>> streamMap(final Pair<Class<K>,Class<V>> type) {
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
  public static <K extends Object, V extends Object> Stream<Pair<K,V>> stream(final Map<K,V> data) {
    Set<Map.Entry<K,V>> _entrySet = data.entrySet();
    final Function1<Map.Entry<K,V>,Pair<K,V>> _function = new Function1<Map.Entry<K,V>,Pair<K,V>>() {
      public Pair<K,V> apply(final Map.Entry<K,V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        return Pair.<K, V>of(_key, _value);
      }
    };
    Iterable<Pair<K,V>> _map = IterableExtensions.<Map.Entry<K,V>, Pair<K,V>>map(_entrySet, _function);
    return StreamExtensions.<Pair<K,V>>stream(_map);
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
            stream.push(_next);
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
      stream.onReadyForNext(pushNext);
      pushNext.apply();
      _xblockexpression = stream;
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
    return new Finish<T>();
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn
   */
  public static <T extends Object, R extends Object> Stream<R> map(final Stream<T> stream, final Function1<? super T,? extends R> mappingFn) {
    Stream<R> _xblockexpression = null;
    {
      final Stream<R> newStream = new Stream<R>(stream);
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          R _apply = mappingFn.apply(it);
          newStream.push(_apply);
        }
      };
      stream.onNextValue(_function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <T extends Object> Stream<T> filter(final Stream<T> stream, final Function1<? super T,? extends Boolean> filterFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>(stream);
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
      stream.onNextValue(_function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Stream<T> split(final Stream<T> stream, final Function1<? super T,? extends Boolean> splitConditionFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>(stream);
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          Boolean _apply = splitConditionFn.apply(it);
          if ((_apply).booleanValue()) {
            Value<T> _value = new Value<T>(it);
            Stream<T> _doubleLessThan = StreamExtensions.<T>operator_doubleLessThan(newStream, _value);
            Finish<T> _finish = new Finish<T>();
            StreamExtensions.<T>operator_doubleLessThan(_doubleLessThan, _finish);
            newStream.publish();
          } else {
            newStream.push(it);
          }
        }
      };
      stream.onNextValue(_function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create from an existing stream a stream of streams, separated by finishes in the start stream.
   */
  public static <T extends Object> Stream<Stream<T>> substream(final Stream<T> stream) {
    Stream<Stream<T>> _xblockexpression = null;
    {
      final Stream<Stream<T>> newStream = new Stream<Stream<T>>(stream);
      Stream<T> _stream = new Stream<T>();
      final AtomicReference<Stream<T>> substream = new AtomicReference<Stream<T>>(_stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          Stream<T> _get = substream.get();
          newStream.push(_get);
          Stream<T> _stream = new Stream<T>();
          substream.set(_stream);
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          Stream<T> _get = substream.get();
          _get.push(it);
        }
      };
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Only let pass a certain amount of items through the stream
   */
  public static <T extends Object> Stream<T> limit(final Stream<T> stream, final int amount) {
    final Function2<T,Long,Boolean> _function = new Function2<T,Long,Boolean>() {
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
  public static <T extends Object> Stream<T> until(final Stream<T> stream, final Function1<? super T,? extends Boolean> untilFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>(stream);
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
      stream.onNextValue(_function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * Passes a counter as second parameter to the untilFn, starting at 1.
   */
  public static <T extends Object> Stream<T> until(final Stream<T> stream, final Function2<? super T,? super Long,? extends Boolean> untilFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicLong count = new AtomicLong(0);
      final Stream<T> newStream = new Stream<T>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          count.set(0);
          newStream.finish();
        }
      };
      Stream<T> _onNextFinish = stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
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
      _onNextFinish.onNextValue(_function_1);
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
   * Allows concurrency promises to be resolved in parallel.
   */
  public static <T extends Object, R extends Object> Stream<T> resolve(final Stream<Promise<T>> stream, final int concurrency) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicInteger processes = new AtomicInteger(0);
      final Stream<T> newStream = new Stream<T>(stream);
      final Procedure1<Promise<T>> _function = new Procedure1<Promise<T>>() {
        public void apply(final Promise<T> promise) {
          processes.incrementAndGet();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
              int _decrementAndGet = processes.decrementAndGet();
              boolean _lessThan = (_decrementAndGet < concurrency);
              if (_lessThan) {
                stream.next();
              }
            }
          };
          Promise<T> _onError = promise.onError(_function);
          final Procedure1<T> _function_1 = new Procedure1<T>() {
            public void apply(final T it) {
              newStream.push(it);
              int _decrementAndGet = processes.decrementAndGet();
              boolean _lessThan = (_decrementAndGet < concurrency);
              if (_lessThan) {
                stream.next();
              }
            }
          };
          _onError.then(_function_1);
        }
      };
      stream.onNextValue(_function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   */
  public static <T extends Object> void onEach(final Stream<T> stream, final Procedure1<? super T> listener) {
    final Procedure1<T> _function = new Procedure1<T>() {
      public void apply(final T it) {
        try {
          listener.apply(it);
        } finally {
          stream.next();
        }
      }
    };
    stream.onNextValue(_function);
    stream.next();
  }
  
  /**
   * Processes a stream of tasks. It returns a new stream that allows you to listen for errors,
   * and that pushes thr number of tasks that were performed when the source stream of tasks
   * finishes.
   * <pre>
   * def Task doSomeTask(int userId) {
   * 		task [ doSomeTask |
   * 			..do some asynchronous work here..
   * 		]
   * }
   * 
   * val userIds = #[5, 6, 2, 67]
   * userIds.stream
   * 		.map [ doSomeTask ]
   * 		.process(3) // 3 parallel processes max
   * </pre>
   */
  public static <T extends Object, R extends Object> Stream<Long> process(final Stream<Task> stream, final int concurrency) {
    Stream<Long> _xblockexpression = null;
    {
      final AtomicInteger processes = new AtomicInteger(0);
      final AtomicLong count = new AtomicLong(0);
      final Stream<Long> newStream = new Stream<Long>(stream);
      final Procedure1<Task> _function = new Procedure1<Task>() {
        public void apply(final Task promise) {
          processes.incrementAndGet();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
              int _decrementAndGet = processes.decrementAndGet();
              boolean _lessThan = (_decrementAndGet < concurrency);
              if (_lessThan) {
                stream.next();
              }
            }
          };
          Promise<Boolean> _onError = promise.onError(_function);
          final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
            public void apply(final Boolean it) {
              count.incrementAndGet();
              int _decrementAndGet = processes.decrementAndGet();
              boolean _lessThan = (_decrementAndGet < concurrency);
              if (_lessThan) {
                stream.next();
              }
            }
          };
          _onError.then(_function_1);
        }
      };
      stream.onNextValue(_function);
      final Procedure0 _function_1 = new Procedure0() {
        public void apply() {
          long _get = count.get();
          newStream.push(Long.valueOf(_get));
          count.set(0);
          stream.next();
        }
      };
      stream.onNextFinish(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Processes a stream of tasks. It returns a new stream that allows you to listen for errors,
   * and that pushes the number of tasks that were performed when the source stream of tasks
   * finishes. Performs at most one task at a time.
   * 
   * @see StreamExtensions.process(Stream<Task> stream, int concurrency)
   */
  public static <T extends Object, R extends Object> Stream<Long> process(final Stream<Task> stream) {
    return StreamExtensions.<Object, Object>process(stream, 1);
  }
  
  /**
   * Synchronous listener to the stream for finishes. Automatically requests the next entry.
   */
  public static <T extends Object> Stream<T> onFinish(final Stream<T> stream, final Procedure1<? super Void> listener) {
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        try {
          listener.apply(null);
        } finally {
          stream.next();
        }
      }
    };
    return stream.onNextFinish(_function);
  }
  
  /**
   * Synchronous listener to the stream for finishes. Automatically requests the next entry.
   */
  public static <T extends Object, R extends Object> Stream<T> onFinishAsync(final Stream<T> stream, final Function1<? super Void,? extends Promise<R>> listener) {
    final Procedure0 _function = new Procedure0() {
      public void apply() {
        try {
          Promise<R> _apply = listener.apply(null);
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              stream.error(it);
              stream.next();
            }
          };
          Promise<R> _onError = _apply.onError(_function);
          final Procedure1<R> _function_1 = new Procedure1<R>() {
            public void apply(final R it) {
              stream.next();
            }
          };
          _onError.then(_function_1);
        } finally {
          stream.next();
        }
      }
    };
    return stream.onNextFinish(_function);
  }
  
  /**
   * Synchronous listener to the stream for errors. Automatically requests the next entry.
   */
  public static <T extends Object> Stream<T> onError(final Stream<T> stream, final Procedure1<? super Throwable> listener) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        try {
          listener.apply(null);
        } finally {
          stream.next();
        }
      }
    };
    return stream.onNextError(_function);
  }
  
  /**
   * Create a new stream that listenes to this stream
   */
  public static <T extends Object> Stream<T> fork(final Stream<T> stream) {
    final Function1<T,T> _function = new Function1<T,T>() {
      public T apply(final T it) {
        return it;
      }
    };
    return StreamExtensions.<T, T>map(stream, _function);
  }
  
  /**
   * Forward the results of the stream to another stream and start that stream.
   */
  public static <T extends Object> void forwardTo(final Stream<T> stream, final Stream<T> otherStream) {
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      public void apply(final Throwable it) {
        otherStream.error(it);
        stream.next();
      }
    };
    stream.onNextError(_function);
    final Procedure0 _function_1 = new Procedure0() {
      public void apply() {
        otherStream.finish();
        stream.next();
      }
    };
    stream.onNextFinish(_function_1);
    final Procedure1<T> _function_2 = new Procedure1<T>() {
      public void apply(final T it) {
        otherStream.push(it);
        stream.next();
      }
    };
    stream.onNextValue(_function_2);
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   */
  public static <T extends Object> Promise<T> first(final Stream<T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Procedure1<T> _function = new Procedure1<T>() {
        public void apply(final T it) {
          boolean _isFulfilled = promise.isFulfilled();
          boolean _not = (!_isFulfilled);
          if (_not) {
            promise.set(it);
          }
        }
      };
      stream.onNextValue(_function);
      final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
        public void apply(final Throwable it) {
          boolean _isFulfilled = promise.isFulfilled();
          boolean _not = (!_isFulfilled);
          if (_not) {
            promise.error(it);
          }
        }
      };
      StreamExtensions.<T>onError(stream, _function_1);
      stream.next();
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
  
  /**
   * Collect all items from a stream, separated by finishes
   */
  public static <T extends Object> Stream<List<T>> collect(final Stream<T> stream) {
    Stream<List<T>> _xblockexpression = null;
    {
      LinkedList<T> _linkedList = new LinkedList<T>();
      final AtomicReference<LinkedList<T>> list = new AtomicReference<LinkedList<T>>(_linkedList);
      final Stream<List<T>> newStream = new Stream<List<T>>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          final LinkedList<T> collected = list.get();
          LinkedList<T> _linkedList = new LinkedList<T>();
          list.set(_linkedList);
          newStream.push(collected);
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          LinkedList<T> _get = list.get();
          _get.add(it);
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
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
      final Stream<Double> newStream = new Stream<Double>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          final double collected = sum.doubleValue();
          sum.set(0);
          newStream.push(Double.valueOf(collected));
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          double _doubleValue = it.doubleValue();
          sum.addAndGet(_doubleValue);
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Average the items in the stream until a finish
   */
  public static <T extends Number> Stream<Double> avg(final Stream<T> stream) {
    Stream<Double> _xblockexpression = null;
    {
      final AtomicDouble avg = new AtomicDouble();
      final AtomicLong count = new AtomicLong(0);
      final Stream<Double> newStream = new Stream<Double>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          double _doubleValue = avg.doubleValue();
          long _andSet = count.getAndSet(0);
          final double collected = (_doubleValue / _andSet);
          avg.set(0);
          newStream.push(Double.valueOf(collected));
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          double _doubleValue = it.doubleValue();
          avg.addAndGet(_doubleValue);
          count.incrementAndGet();
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
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
      final Stream<Long> newStream = new Stream<Long>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          long _andSet = count.getAndSet(0);
          newStream.push(Long.valueOf(_andSet));
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          count.incrementAndGet();
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value until a finish.
   */
  public static <T extends Object> Stream<T> reduce(final Stream<T> stream, final T initial, final Function2<? super T,? super T,? extends T> reducerFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> reduced = new AtomicReference<T>(initial);
      final Stream<T> newStream = new Stream<T>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          T _andSet = reduced.getAndSet(initial);
          newStream.push(_andSet);
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          T _get = reduced.get();
          T _apply = reducerFn.apply(_get, it);
          reduced.set(_apply);
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * The counter is the count of the incoming stream entry (since the start or the last finish)
   */
  public static <T extends Object> Stream<T> reduce(final Stream<T> stream, final T initial, final Function3<? super T,? super T,? super Long,? extends T> reducerFn) {
    Stream<T> _xblockexpression = null;
    {
      final AtomicReference<T> reduced = new AtomicReference<T>(initial);
      final AtomicLong count = new AtomicLong(0);
      final Stream<T> newStream = new Stream<T>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          final T result = reduced.getAndSet(initial);
          count.set(0);
          newStream.push(result);
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
        public void apply(final T it) {
          T _get = reduced.get();
          long _andIncrement = count.getAndIncrement();
          T _apply = reducerFn.apply(_get, it, Long.valueOf(_andIncrement));
          reduced.set(_apply);
          stream.next();
        }
      };
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * True if any of the values match the passed testFn
   * FIX: currently .first gives recursive loop?
   */
  public static <T extends Object> Stream<Boolean> anyMatch(final Stream<T> stream, final Function1<? super T,? extends Boolean> testFn) {
    Stream<Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final Stream<Boolean> newStream = new Stream<Boolean>(stream);
      final Procedure0 _function = new Procedure0() {
        public void apply() {
          final boolean matched = anyMatch.get();
          anyMatch.set(false);
          if ((!matched)) {
            newStream.push(Boolean.valueOf(false));
          }
        }
      };
      stream.onNextFinish(_function);
      final Procedure1<T> _function_1 = new Procedure1<T>() {
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
      stream.onNextValue(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
}
