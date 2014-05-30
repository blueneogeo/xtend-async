package nl.kii.stream;

import com.google.common.collect.Lists;
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
import nl.kii.stream.AsyncSubscription;
import nl.kii.stream.CommandSubscription;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.SyncSubscription;
import nl.kii.stream.Value;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
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
  
  static <T extends Object, R extends Object> CommandSubscription connectTo(final Stream<T> newStream, final AsyncSubscription<?> parent) {
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<R, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              counter.set(0);
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<R, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> Stream<T> split(final Stream<T> stream, final Function1<? super T, ? extends Boolean> splitConditionFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Boolean _apply = splitConditionFn.apply(it);
              if ((_apply).booleanValue()) {
                Value<T> _value = new Value<T>(it);
                Finish<T> _finish = new Finish<T>();
                final List<? extends Entry<T>> entries = Collections.<Entry<T>>unmodifiableList(Lists.<Entry<T>>newArrayList(_value, _finish));
                Entries<T> _entries = new Entries<T>(((Entry<T>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              } else {
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
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
      final Stream<Stream<T>> newStream = new Stream<Stream<T>>();
      Stream<T> _stream = new Stream<T>();
      final AtomicReference<Stream<T>> substream = new AtomicReference<Stream<T>>(_stream);
      final Procedure1<AsyncSubscription<T>> _function = new Procedure1<AsyncSubscription<T>>() {
        public void apply(final AsyncSubscription<T> it) {
          final Procedure1<T> _function = new Procedure1<T>() {
            public void apply(final T it) {
              Stream<T> _get = substream.get();
              _get.push(it);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              Stream<T> _get = substream.get();
              newStream.push(_get);
              Stream<T> _stream = new Stream<T>();
              substream.set(_stream);
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<Stream<T>, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              count.set(0);
              newStream.finish();
            }
          };
          it.finish(_function_2);
        }
      };
      final AsyncSubscription<T> subscription = StreamExtensions.<T>onAsync(stream, _function);
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
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
              final Procedure1<T> _function = new Procedure1<T>() {
                public void apply(final T it) {
                  newStream.push(it);
                  onProcessComplete.apply();
                }
              };
              promise.then(_function);
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newStream.error(it);
            }
          };
          it.error(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            public void apply(final Void it) {
              int _get = processes.get();
              boolean _equals = (_get == 0);
              if (_equals) {
                newStream.finish();
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
  
  public static <T extends Object> SyncSubscription<T> on(final Stream<T> stream, final Procedure1<? super SyncSubscription<T>> handlerFn) {
    SyncSubscription<T> _xblockexpression = null;
    {
      final SyncSubscription<T> handler = new SyncSubscription<T>(stream);
      handlerFn.apply(handler);
      stream.next();
      _xblockexpression = handler;
    }
    return _xblockexpression;
  }
  
  public static <T extends Object> AsyncSubscription<T> onAsync(final Stream<T> stream, final Procedure1<? super AsyncSubscription<T>> handlerFn) {
    AsyncSubscription<T> _xblockexpression = null;
    {
      final AsyncSubscription<T> handler = new AsyncSubscription<T>(stream);
      handlerFn.apply(handler);
      _xblockexpression = handler;
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
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * note: onEach swallows exceptions in your listener. If you needs error detection/handling, use .on[] instead.
   */
  public static <T extends Object> SyncSubscription<T> onEach(final Stream<T> stream, final Procedure1<? super T> listener) {
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
    return StreamExtensions.<T>on(stream, _function);
  }
  
  /**
   * Create a new stream that listenes to this stream
   */
  public static <T extends Object> Stream<T> fork(final Stream<T> stream) {
    final Function1<T, T> _function = new Function1<T, T>() {
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
        final Procedure1<Void> _function_2 = new Procedure1<Void>() {
          public void apply(final Void it) {
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
              boolean _isFulfilled = promise.isFulfilled();
              boolean _not = (!_isFulfilled);
              if (_not) {
                promise.set(it);
              }
            }
          };
          it.each(_function);
          final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              boolean _isFulfilled = promise.isFulfilled();
              boolean _not = (!_isFulfilled);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              final LinkedList<T> collected = list.get();
              LinkedList<T> _linkedList = new LinkedList<T>();
              list.set(_linkedList);
              newStream.push(collected);
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
      StreamExtensions.<List<T>, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              final double collected = sum.doubleValue();
              sum.set(0);
              newStream.push(Double.valueOf(collected));
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
      StreamExtensions.<Double, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              double _doubleValue = avg.doubleValue();
              long _andSet = count.getAndSet(0);
              final double collected = (_doubleValue / _andSet);
              avg.set(0);
              newStream.push(Double.valueOf(collected));
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
      StreamExtensions.<Double, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              long _andSet = count.getAndSet(0);
              newStream.push(Long.valueOf(_andSet));
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
      StreamExtensions.<Long, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              T _andSet = reduced.getAndSet(initial);
              newStream.push(_andSet);
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
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              final T result = reduced.getAndSet(initial);
              count.set(0);
              newStream.push(result);
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
      StreamExtensions.<T, Object>connectTo(newStream, subscription);
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
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              final boolean matched = anyMatch.get();
              anyMatch.set(false);
              if ((!matched)) {
                newStream.push(Boolean.valueOf(false));
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
      StreamExtensions.<Boolean, Object>connectTo(newStream, subscription);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
}
