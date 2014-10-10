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
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.stream.BaseStream;
import nl.kii.stream.Close;
import nl.kii.stream.Closed;
import nl.kii.stream.Entries;
import nl.kii.stream.Entry;
import nl.kii.stream.Finish;
import nl.kii.stream.IStream;
import nl.kii.stream.Next;
import nl.kii.stream.Overflow;
import nl.kii.stream.Skip;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamException;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamNotification;
import nl.kii.stream.StreamObserver;
import nl.kii.stream.StreamResponder;
import nl.kii.stream.StreamResponderBuilder;
import nl.kii.stream.SubStream;
import nl.kii.stream.UncaughtStreamException;
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
          StreamExtensions.<T, T>pipe(_stream, newStream);
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
            StreamExtensions.<T, T>operator_doubleGreaterThan(_next, stream);
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
      StreamExtensions.<T, T>monitor(stream, _function_1);
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
              boolean _isOpen = newStream.isOpen();
              boolean _not = (!_isOpen);
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
        StreamExtensions.<List<Byte>, List<Byte>>monitor(newStream, _function);
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
              boolean _isOpen = newStream.isOpen();
              if (_isOpen) {
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
      StreamExtensions.<Integer, Integer>monitor(newStream, _function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Observe the entries coming off this stream using a StreamObserver.
   * Note that you can only have ONE stream observer for every stream!
   * If you want more than one observer, you can split the stream.
   * <p>
   * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
   * instead, for a more concise and elegant builder syntax.
   */
  public static <R extends Object, T extends Object> void observe(final IStream<R, T> stream, final StreamObserver<R, T> observer) {
    stream.setOperation("observe");
    final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
      public void apply(final Entry<R, T> entry) {
        final Entry<R, T> it = entry;
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            observer.onValue(((Value<R, T>)it).from, ((Value<R, T>)it).value);
          }
        }
        if (!_matched) {
          if (it instanceof Finish) {
            _matched=true;
            observer.onFinish(((Finish<R, T>)it).from, ((Finish<R, T>)it).level);
          }
        }
        if (!_matched) {
          if (it instanceof nl.kii.stream.Error) {
            _matched=true;
            observer.onError(((nl.kii.stream.Error<R, T>)it).from, ((nl.kii.stream.Error<R, T>)it).error);
          }
        }
        if (!_matched) {
          if (it instanceof Closed) {
            _matched=true;
            observer.onClosed();
          }
        }
      }
    };
    stream.onChange(_function);
  }
  
  /**
   * Monitor commands given to the stream.
   */
  public static <R extends Object, T extends Object> void monitor(final IStream<R, T> stream, final StreamMonitor monitor) {
    final Procedure1<StreamNotification> _function = new Procedure1<StreamNotification>() {
      public void apply(final StreamNotification notification) {
        final StreamNotification it = notification;
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Next) {
            _matched=true;
            monitor.onNext();
          }
        }
        if (!_matched) {
          if (it instanceof Skip) {
            _matched=true;
            monitor.onSkip();
          }
        }
        if (!_matched) {
          if (it instanceof Close) {
            _matched=true;
            monitor.onClose();
          }
        }
        if (!_matched) {
          if (it instanceof Overflow) {
            _matched=true;
            monitor.onOverflow(((Overflow)it).entry);
          }
        }
      }
    };
    stream.onNotify(_function);
  }
  
  /**
   * Create a publisher for the stream. This allows you to observe the stream
   * with multiple listeners. Publishers do not support flow control, and the
   * created Publisher will eagerly pull all data from the stream for publishing.
   */
  public static <R extends Object, T extends Object> Publisher<T> publish(final IStream<R, T> stream) {
    Publisher<T> _xblockexpression = null;
    {
      final Publisher<T> publisher = new Publisher<T>();
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              publisher.apply($1);
              Boolean _publishing = publisher.getPublishing();
              if ((_publishing).booleanValue()) {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            public void apply(final Void it) {
              publisher.setPublishing(Boolean.valueOf(false));
            }
          };
          it.closed(_function_1);
        }
      };
      StreamExtensions.<R, T>on(stream, _function);
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
      StreamExtensions.<T, T>monitor(newStream, _function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleGreaterThan(final R value, final IStream<R, T> stream) {
    IStream<R, T> _xblockexpression = null;
    {
      stream.push(value);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleLessThan(final IStream<R, T> stream, final R value) {
    IStream<R, T> _xblockexpression = null;
    {
      stream.push(value);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleGreaterThan(final List<R> value, final IStream<R, T> stream) {
    IStream<R, T> _xblockexpression = null;
    {
      final Procedure1<R> _function = new Procedure1<R>() {
        public void apply(final R it) {
          stream.push(it);
        }
      };
      IterableExtensions.<R>forEach(value, _function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleLessThan(final IStream<R, T> stream, final List<R> value) {
    IStream<R, T> _xblockexpression = null;
    {
      final Procedure1<R> _function = new Procedure1<R>() {
        public void apply(final R it) {
          stream.push(it);
        }
      };
      IterableExtensions.<R>forEach(value, _function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add an entry to a stream (such as error or finish)
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleLessThan(final IStream<R, T> stream, final Entry<R, T> entry) {
    IStream<R, T> _xblockexpression = null;
    {
      Stream<R> _root = stream.getRoot();
      _root.apply(entry);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the << operator
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleLessThan(final IStream<R, T> stream, final Throwable t) {
    IStream<R, T> _xblockexpression = null;
    {
      Stream<R> _root = stream.getRoot();
      nl.kii.stream.Error<R, T> _error = new nl.kii.stream.Error<R, T>(null, t);
      _root.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the >> operator
   */
  public static <R extends Object, T extends Object> IStream<R, T> operator_doubleGreaterThan(final Throwable t, final IStream<R, T> stream) {
    IStream<R, T> _xblockexpression = null;
    {
      Stream<R> _root = stream.getRoot();
      nl.kii.stream.Error<R, T> _error = new nl.kii.stream.Error<R, T>(null, t);
      _root.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <R extends Object, T extends Object> StreamSource<R, T> operator_doubleGreaterThan(final IStream<R, T> source, final IStream<R, T> dest) {
    return StreamExtensions.<R, T>pipe(source, dest);
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <R extends Object, T extends Object> StreamSource<R, T> operator_doubleLessThan(final IStream<R, T> dest, final IStream<R, T> source) {
    return StreamExtensions.<R, T>pipe(source, dest);
  }
  
  /**
   * split a source into a new destination stream
   */
  public static <R extends Object, T extends Object> StreamSource<R, T> operator_doubleGreaterThan(final StreamSource<R, T> source, final IStream<R, T> dest) {
    return source.pipe(dest);
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <R extends Object, T extends Object> Finish<R, T> finish() {
    return new Finish<R, T>(null, 0);
  }
  
  public static <R extends Object, T extends Object> Finish<R, T> finish(final int level) {
    return new Finish<R, T>(null, level);
  }
  
  public static <R extends Object, R2 extends Object, T extends Object, T2 extends Object> void controls(final IStream<R, T2> newStream, final IStream<R2, T> parent) {
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
    StreamExtensions.<R, T2>monitor(newStream, _function);
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
  public static <R extends Object, T extends Object, P extends Object> SubStream<R, P> map(final IStream<R, T> stream, final Function1<? super T, ? extends P> mappingFn) {
    SubStream<R, P> _xblockexpression = null;
    {
      final SubStream<R, P> newStream = new SubStream<R, P>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              final P mapped = mappingFn.apply($1);
              newStream.push($0, mapped);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      StreamExtensions.<R, R, T, P>controls(newStream, stream);
      stream.setOperation("map");
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> filter(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> filterFn) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return filterFn.apply(it);
      }
    };
    return StreamExtensions.<R, T>filter(stream, _function);
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> filter(final IStream<R, T> stream, final Function3<? super T, ? super Long, ? super Long, ? extends Boolean> filterFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      Stream<R> _root = stream.getRoot();
      final SubStream<R, T> newStream = new SubStream<R, T>(_root);
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              final long i = index.incrementAndGet();
              long _get = passed.get();
              Boolean _apply = filterFn.apply($1, Long.valueOf(i), Long.valueOf(_get));
              if ((_apply).booleanValue()) {
                passed.incrementAndGet();
                newStream.push($0, $1);
              } else {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              index.set(0);
              passed.set(0);
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
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
  public static <R extends Object, T extends Object> SubStream<R, T> split(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> splitConditionFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final AtomicBoolean justPostedFinish0 = new AtomicBoolean(false);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Boolean _apply = splitConditionFn.apply($1);
              if ((_apply).booleanValue()) {
                Value<R, T> _value = new Value<R, T>($0, $1);
                Finish<R, T> _finish = new Finish<R, T>($0, 0);
                final List<? extends Entry<R, T>> entries = Collections.<Entry<R, T>>unmodifiableList(CollectionLiterals.<Entry<R, T>>newArrayList(_value, _finish));
                justPostedFinish0.set(true);
                Entries<R, T> _entries = new Entries<R, T>(((Entry<R, T>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              } else {
                justPostedFinish0.set(false);
                Value<R, T> _value_1 = new Value<R, T>($0, $1);
                newStream.apply(_value_1);
              }
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              boolean _get = justPostedFinish0.get();
              if (_get) {
                Finish<R, Object> _finish = new Finish<R, Object>($0, (($1).intValue() + 1));
                newStream.apply(_finish);
              } else {
                justPostedFinish0.set(true);
                Finish<R, Object> _finish_1 = new Finish<R, Object>($0, 0);
                Finish<R, Object> _finish_2 = new Finish<R, Object>($0, (($1).intValue() + 1));
                final List<Finish<R, Object>> entries = Collections.<Finish<R, Object>>unmodifiableList(CollectionLiterals.<Finish<R, Object>>newArrayList(_finish_1, _finish_2));
                Entries<R, Object> _entries = new Entries<R, Object>(((Entry<R, Object>[])Conversions.unwrapArray(entries, Entry.class)));
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("split");
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Merges one level of finishes.
   * @see StreamExtensions.split
   */
  public static <R extends Object, T extends Object> SubStream<R, T> merge(final IStream<R, T> stream) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Value<R, T> _value = new Value<R, T>($0, $1);
              newStream.apply(_value);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() > 0)) {
                newStream.finish($0, (($1).intValue() - 1));
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("merge");
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Only let pass a certain amount of items through the stream
   */
  public static <R extends Object, T extends Object> SubStream<R, T> limit(final IStream<R, T> stream, final int amount) {
    final Function2<T, Long, Boolean> _function = new Function2<T, Long, Boolean>() {
      public Boolean apply(final T it, final Long c) {
        return Boolean.valueOf(((c).longValue() > amount));
      }
    };
    SubStream<R, T> _until = StreamExtensions.<R, T>until(stream, _function);
    final Procedure1<SubStream<R, T>> _function_1 = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_until, _function_1);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> until(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> untilFn) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return untilFn.apply(it);
      }
    };
    return StreamExtensions.<R, T>until(stream, _function);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> until(final IStream<R, T> stream, final Function3<? super T, ? super Long, ? super Long, ? extends Boolean> untilFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              final long i = index.incrementAndGet();
              long _get = passed.get();
              Boolean _apply = untilFn.apply($1, Long.valueOf(i), Long.valueOf(_get));
              if ((_apply).booleanValue()) {
                passed.incrementAndGet();
                stream.skip();
                stream.next();
              } else {
                newStream.push($0, $1);
              }
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              index.set(0);
              passed.set(0);
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * Passes a counter as second parameter to the untilFn, starting at 1.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> until(final IStream<R, T> stream, final Function2<? super T, ? super Long, ? extends Boolean> untilFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      final AtomicLong count = new AtomicLong(0);
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              long _incrementAndGet = count.incrementAndGet();
              Boolean _apply = untilFn.apply($1, Long.valueOf(_incrementAndGet));
              if ((_apply).booleanValue()) {
                stream.skip();
                stream.next();
              } else {
                newStream.push($0, $1);
              }
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              count.set(0);
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Flatten a stream of streams into a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <R extends Object, R2 extends Object, T extends Object, S extends IStream<R2, T>> SubStream<R, T> flatten(final IStream<R, S> stream) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, S>> _function = new Procedure1<StreamHandlerBuilder<R, S>>() {
        public void apply(final StreamHandlerBuilder<R, S> it) {
          final Procedure2<R, S> _function = new Procedure2<R, S>() {
            public void apply(final R r, final S s) {
              final Procedure1<StreamHandlerBuilder<R2, T>> _function = new Procedure1<StreamHandlerBuilder<R2, T>>() {
                public void apply(final StreamHandlerBuilder<R2, T> it) {
                  final Procedure2<R2, T> _function = new Procedure2<R2, T>() {
                    public void apply(final R2 $0, final T $1) {
                      newStream.push(r, $1);
                      s.next();
                    }
                  };
                  it.each(_function);
                  final Procedure2<R2, Throwable> _function_1 = new Procedure2<R2, Throwable>() {
                    public void apply(final R2 $0, final Throwable $1) {
                      newStream.error(r, $1);
                      s.next();
                    }
                  };
                  it.error(_function_1);
                }
              };
              StreamExtensions.<R2, T>on(s, _function);
              s.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
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
      StreamExtensions.<R, S>on(stream, _function);
      stream.setOperation("flatten");
      StreamExtensions.<R, R, S, T>controls(newStream, stream);
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
  public static <R extends Object, T extends Object, P extends Object> SubStream<R, P> flatMap(final IStream<R, T> stream, final Function1<? super T, ? extends IStream<R, P>> mapFn) {
    SubStream<R, IStream<R, P>> _map = StreamExtensions.<R, T, IStream<R, P>>map(stream, mapFn);
    SubStream<R, P> _flatten = StreamExtensions.<R, R, P, IStream<R, P>>flatten(_map);
    final Procedure1<SubStream<R, P>> _function = new Procedure1<SubStream<R, P>>() {
      public void apply(final SubStream<R, P> it) {
        stream.setOperation("flatmap");
      }
    };
    return ObjectExtensions.<SubStream<R, P>>operator_doubleArrow(_flatten, _function);
  }
  
  /**
   * Keep count of how many items have been streamed so far, and for each
   * value from the original stream, push a pair of count->value.
   * Finish(0) resets the count.
   */
  public static <R extends Object, T extends Object> SubStream<R, Pair<Integer, T>> index(final IStream<R, T> stream) {
    SubStream<R, Pair<Integer, T>> _xblockexpression = null;
    {
      final SubStream<R, Pair<Integer, T>> newStream = new SubStream<R, Pair<Integer, T>>(stream);
      final AtomicInteger counter = new AtomicInteger(0);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              int _incrementAndGet = counter.incrementAndGet();
              Pair<Integer, T> _mappedTo = Pair.<Integer, T>of(Integer.valueOf(_incrementAndGet), $1);
              newStream.push($0, _mappedTo);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                counter.set(0);
              }
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("index");
      StreamExtensions.<R, R, T, Pair<Integer, T>>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a splitter StreamSource from a stream that lets you listen to the stream
   * with multiple listeners.
   */
  public static <R extends Object, T extends Object> StreamCopySplitter<R, T> split(final IStream<R, T> stream) {
    return new StreamCopySplitter<R, T>(stream);
  }
  
  /**
   * Balance the stream into multiple listening streams.
   */
  public static <T extends Object> LoadBalancer<T, T> balance(final Stream<T> stream) {
    return new LoadBalancer<T, T>(stream);
  }
  
  /**
   * Tell the stream what size its buffer should be, and what should happen in case
   * of a buffer overflow.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> buffer(final IStream<R, T> stream, final int maxSize, final Procedure1<? super Entry<?, ?>> onOverflow) {
    SubStream<R, T> _xblockexpression = null;
    {
      boolean _matched = false;
      if (!_matched) {
        if (stream instanceof BaseStream) {
          _matched=true;
          ((BaseStream<R, T>)stream).setMaxBufferSize(Integer.valueOf(maxSize));
        }
      }
      final SubStream<R, T> newStream = new SubStream<R, T>(stream, maxSize);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              stream.next();
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
          final Procedure1<Entry<?, ?>> _function_3 = new Procedure1<Entry<?, ?>>() {
            public void apply(final Entry<?, ?> it) {
              onOverflow.apply(it);
            }
          };
          it.overflow(_function_3);
        }
      };
      StreamExtensions.<R, T>monitor(newStream, _function_1);
      final Procedure1<StreamResponder> _function_2 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Entry<?, ?>> _function = new Procedure1<Entry<?, ?>>() {
            public void apply(final Entry<?, ?> it) {
              onOverflow.apply(it);
            }
          };
          it.overflow(_function);
        }
      };
      StreamExtensions.<R, T>monitor(stream, _function_2);
      final Procedure1<SubStream<R, T>> _function_3 = new Procedure1<SubStream<R, T>>() {
        public void apply(final SubStream<R, T> it) {
          String _operation = stream.getOperation();
          it.setOperation(_operation);
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(newStream, _function_3);
    }
    return _xblockexpression;
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds to pass through the stream.
   * All other values are dropped.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> throttle(final IStream<R, T> stream, final long periodMs) {
    SubStream<R, T> _xblockexpression = null;
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
      SubStream<R, T> _filter = StreamExtensions.<R, T>filter(stream, _function);
      final Procedure1<SubStream<R, T>> _function_1 = new Procedure1<SubStream<R, T>>() {
        public void apply(final SubStream<R, T> it) {
          stream.setOperation((("throttle(periodMs=" + Long.valueOf(periodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_filter, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds to pass through the stream.
   * All other values are buffered, and dropped only after the buffer has reached a given size.
   * This method requires a timer function, that takes a period in milliseconds, and that calls
   * the passed procedure when that period has expired.
   * <p>
   * Since ratelimiting does not throw away values when the source is pushing data faster
   * than can be processed, it can cause buffer overflow after some time.
   * <p>
   * You are strongly adviced to put a buffer statement before the ratelimit so you can
   * set the max buffer size and handle overflowing data, like this:
   * <p>
   * <pre>
   * someStream
   *    .buffer(1000) [ println('overflow error! handle this') ]
   *    .ratelimit(100) [ period, timeoutFn, | vertx.setTimer(period, timeoutFn) ]
   *    .onEach [ ... ]
   * </pre>
   */
  public static <R extends Object, T extends Object> SubStream<R, T> ratelimit(final IStream<R, T> stream, final long periodMs, final Procedure2<? super Long, ? super Procedure0> timerFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      final AtomicLong lastNextMs = new AtomicLong((-1));
      final AtomicBoolean isTiming = new AtomicBoolean();
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<Entry<R, T>> _function = new Procedure1<Entry<R, T>>() {
        public void apply(final Entry<R, T> it) {
          newStream.apply(it);
        }
      };
      stream.onChange(_function);
      final Procedure1<StreamResponder> _function_1 = new Procedure1<StreamResponder>() {
        public void apply(final StreamResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              final long now = System.currentTimeMillis();
              boolean _or = false;
              long _get = lastNextMs.get();
              boolean _equals = (_get == (-1));
              if (_equals) {
                _or = true;
              } else {
                long _get_1 = lastNextMs.get();
                long _minus = (now - _get_1);
                boolean _greaterThan = (_minus > periodMs);
                _or = _greaterThan;
              }
              if (_or) {
                lastNextMs.set(now);
                stream.next();
              } else {
                boolean _get_2 = isTiming.get();
                boolean _not = (!_get_2);
                if (_not) {
                  long _get_3 = lastNextMs.get();
                  final long delayMs = ((now + periodMs) - _get_3);
                  final Procedure0 _function = new Procedure0() {
                    public void apply() {
                      isTiming.set(false);
                      final long now2 = System.currentTimeMillis();
                      lastNextMs.set(now2);
                      stream.next();
                    }
                  };
                  timerFn.apply(Long.valueOf(delayMs), _function);
                }
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
      StreamExtensions.<R, T>monitor(newStream, _function_1);
      final Procedure1<SubStream<R, T>> _function_2 = new Procedure1<SubStream<R, T>>() {
        public void apply(final SubStream<R, T> it) {
          stream.setOperation((("ratelimit(periodMs=" + Long.valueOf(periodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(newStream, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
   * <p>
   * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> synchronizeWith(final IStream<R, T> stream, final IStream<?, ?> timerStream) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<?, ?>> _function = new Procedure1<StreamHandlerBuilder<?, ?>>() {
        public void apply(final StreamHandlerBuilder<?, ?> it) {
          final Procedure2<Object, Throwable> _function = new Procedure2<Object, Throwable>() {
            public void apply(final Object $0, final Throwable $1) {
              newStream.error(null, $1);
              timerStream.next();
            }
          };
          it.error(_function);
          final Procedure2<Object, Integer> _function_1 = new Procedure2<Object, Integer>() {
            public void apply(final Object $0, final Integer $1) {
              newStream.finish();
              timerStream.next();
            }
          };
          it.finish(_function_1);
          final Procedure2<Object, Object> _function_2 = new Procedure2<Object, Object>() {
            public void apply(final Object $0, final Object $1) {
              boolean _isOpen = stream.isOpen();
              if (_isOpen) {
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
      final Procedure1<StreamHandlerBuilder<R, T>> _function_1 = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
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
      StreamExtensions.<R, T>on(stream, _function_1);
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
      StreamExtensions.<R, T>monitor(newStream, _function_2);
      stream.setOperation("forEvery");
      timerStream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * It only asks the next promise from the stream when the previous promise has been resolved.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> resolve(final IStream<R, ? extends IPromise<T>> stream) {
    SubStream<R, T> _resolve = StreamExtensions.<R, T>resolve(stream, 1);
    final Procedure1<SubStream<R, T>> _function = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        stream.setOperation("resolve");
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * <p>
   * Allows concurrent promises to be resolved in parallel.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> resolve(final IStream<R, ? extends IPromise<T>> stream, final int concurrency) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final AtomicBoolean isFinished = new AtomicBoolean(false);
      final AtomicInteger processes = new AtomicInteger(0);
      final Procedure1<StreamHandlerBuilder<R, ? extends IPromise<T>>> _function = new Procedure1<StreamHandlerBuilder<R, ? extends IPromise<T>>>() {
        public void apply(final StreamHandlerBuilder<R, ? extends IPromise<T>> it) {
          final Procedure2<R, IPromise<T>> _function = new Procedure2<R, IPromise<T>>() {
            public void apply(final R r, final IPromise<T> promise) {
              processes.incrementAndGet();
              final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
                public void apply(final Throwable it) {
                  processes.decrementAndGet();
                  StreamException _streamException = new StreamException("resolve", r, it);
                  newStream.error(_streamException);
                  boolean _get = isFinished.get();
                  if (_get) {
                    newStream.finish(r);
                  }
                }
              };
              IPromise<T> _onError = promise.onError(_function);
              final Procedure1<T> _function_1 = new Procedure1<T>() {
                public void apply(final T it) {
                  processes.decrementAndGet();
                  newStream.push(r, it);
                  boolean _get = isFinished.get();
                  if (_get) {
                    newStream.finish(r);
                  }
                }
              };
              _onError.then(_function_1);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              int _get = processes.get();
              boolean _equals = (_get == 0);
              if (_equals) {
                newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>monitor(newStream, _function_1);
      stream.setOperation((("resolve(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(1)
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> SubStream<T, R> call(final Stream<T> stream, final Function1<? super T, ? extends P> promiseFn) {
    SubStream<T, R> _call = StreamExtensions.<T, R, P>call(stream, 1, promiseFn);
    final Procedure1<SubStream<T, R>> _function = new Procedure1<SubStream<T, R>>() {
      public void apply(final SubStream<T, R> it) {
        stream.setOperation("call");
      }
    };
    return ObjectExtensions.<SubStream<T, R>>operator_doubleArrow(_call, _function);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <T extends Object, R extends Object, P extends IPromise<R>> SubStream<T, R> call(final Stream<T> stream, final int concurrency, final Function1<? super T, ? extends P> promiseFn) {
    SubStream<T, P> _map = StreamExtensions.<T, T, P>map(stream, promiseFn);
    SubStream<T, R> _resolve = StreamExtensions.<T, R>resolve(_map, concurrency);
    final Procedure1<SubStream<T, R>> _function = new Procedure1<SubStream<T, R>>() {
      public void apply(final SubStream<T, R> it) {
        stream.setOperation((("call(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<T, R>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * If an error occurs, call the handler and swallow the error from the stream.
   * @param handler gets the error that was caught.
   * @return a new stream like the incoming stream but without the caught errors.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> onError(final IStream<R, T> stream, final Procedure1<? super Throwable> handler) {
    final Procedure2<R, Throwable> _function = new Procedure2<R, Throwable>() {
      public void apply(final R r, final Throwable err) {
        handler.apply(err);
      }
    };
    return StreamExtensions.<R, T>onError(stream, _function);
  }
  
  /**
   * If an error occurs, call the handler and swallow the error from the stream.
   * @param handler gets the stream input value and the error that was caught.
   * @return a new stream like the incoming stream but without the caught errors.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> onError(final IStream<R, T> stream, final Procedure2<? super R, ? super Throwable> handler) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              handler.apply($0, $1);
              stream.next();
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * If an error occurs, break the stream with an UncaughtStreamException
   */
  public static <R extends Object, T extends Object> SubStream<R, T> onErrorThrow(final IStream<R, T> stream) {
    return StreamExtensions.<R, T>onErrorThrow(stream, "onErrorThrow");
  }
  
  /**
   * If an error occurs, break the stream with an UncaughtStreamException
   */
  public static <R extends Object, T extends Object> SubStream<R, T> onErrorThrow(final IStream<R, T> stream, final String message) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              try {
                throw new UncaughtStreamException(message, $0, $1);
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you respond to the closing of the stream
   */
  public static <R extends Object, T extends Object> SubStream<R, T> onClosed(final IStream<R, T> stream, final Procedure1<? super Void> handler) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, T>on(stream, _function);
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <R extends Object, T extends Object> Task onEach(final IStream<R, T> stream, final Procedure1<? super T> handler) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              handler.apply($1);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              task.error($1);
              stream.next();
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                task.complete();
              }
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("onEach");
      stream.next();
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Performs the task for every value, and only requests the next value from the stream once the task has finished.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <R extends Object, T extends Object, V extends Object, P extends IPromise<V>> Task onEachCall(final IStream<R, T> stream, final Function1<? super T, ? extends P> taskFn) {
    SubStream<R, P> _map = StreamExtensions.<R, T, P>map(stream, taskFn);
    SubStream<R, V> _resolve = StreamExtensions.<R, V>resolve(_map);
    final Procedure1<V> _function = new Procedure1<V>() {
      public void apply(final V it) {
      }
    };
    Task _onEach = StreamExtensions.<R, V>onEach(_resolve, _function);
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
  public static <R extends Object, T extends Object> StreamSource<R, T> pipe(final IStream<R, T> stream, final IStream<R, T> target) {
    StreamCopySplitter<R, T> _split = StreamExtensions.<R, T>split(stream);
    StreamSource<R, T> _pipe = _split.pipe(target);
    final Procedure1<StreamSource<R, T>> _function = new Procedure1<StreamSource<R, T>>() {
      public void apply(final StreamSource<R, T> it) {
        stream.setOperation("pipe");
      }
    };
    return ObjectExtensions.<StreamSource<R, T>>operator_doubleArrow(_pipe, _function);
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Closes the stream once it has the value or an error.
   */
  public static <R extends Object, T extends Object> Promise<T> first(final IStream<R, T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.set($1);
              }
              stream.close();
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.error($1);
              }
              stream.close();
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
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
      StreamExtensions.<R, T>on(stream, _function);
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
  public static <R extends Object, T extends Object> Promise<T> last(final IStream<R, T> stream) {
    Promise<T> _xblockexpression = null;
    {
      final Promise<T> promise = new Promise<T>();
      final AtomicReference<T> last = new AtomicReference<T>();
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                last.set($1);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
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
          final Procedure2<R, Throwable> _function_3 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              stream.next();
            }
          };
          it.error(_function_3);
        }
      };
      StreamExtensions.<R, T>on(stream, _function);
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
  public static <R extends Object, T extends Object> SubStream<R, T> skip(final IStream<R, T> stream, final int amount) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    SubStream<R, T> _filter = StreamExtensions.<R, T>filter(stream, _function);
    final Procedure1<SubStream<R, T>> _function_1 = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        stream.setOperation((("skip(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_filter, _function_1);
  }
  
  /**
   * Take only a set amount of items from the stream.
   * Resets at finish.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> take(final IStream<R, T> stream, final int amount) {
    final Function3<T, Long, Long, Boolean> _function = new Function3<T, Long, Long, Boolean>() {
      public Boolean apply(final T it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    SubStream<R, T> _until = StreamExtensions.<R, T>until(stream, _function);
    final Procedure1<SubStream<R, T>> _function_1 = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_until, _function_1);
  }
  
  /**
   * Start the stream and and promise the first value from it.
   */
  public static <R extends Object, T extends Object> Task then(final IStream<R, T> stream, final Procedure1<T> listener) {
    Promise<T> _first = StreamExtensions.<R, T>first(stream);
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
  public static <R extends Object, T extends Object> void on(final IStream<R, T> stream, final Procedure1<? super StreamHandlerBuilder<R, T>> handlerFn) {
    final Procedure2<IStream<R, T>, StreamHandlerBuilder<R, T>> _function = new Procedure2<IStream<R, T>, StreamHandlerBuilder<R, T>>() {
      public void apply(final IStream<R, T> s, final StreamHandlerBuilder<R, T> builder) {
        handlerFn.apply(builder);
      }
    };
    StreamExtensions.<R, T>on(stream, _function);
  }
  
  public static <R extends Object, T extends Object> void on(final IStream<R, T> stream, final Procedure2<? super IStream<R, T>, ? super StreamHandlerBuilder<R, T>> handlerFn) {
    StreamHandlerBuilder<R, T> _streamHandlerBuilder = new StreamHandlerBuilder<R, T>(stream);
    final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
      public void apply(final StreamHandlerBuilder<R, T> it) {
        final Procedure2<R, T> _function = new Procedure2<R, T>() {
          public void apply(final R $0, final T $1) {
            stream.next();
          }
        };
        it.each(_function);
        final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
          public void apply(final R $0, final Integer $1) {
            stream.next();
          }
        };
        it.finish(_function_1);
        final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
          public void apply(final R $0, final Throwable $1) {
            stream.next();
          }
        };
        it.error(_function_2);
      }
    };
    final StreamHandlerBuilder<R, T> handler = ObjectExtensions.<StreamHandlerBuilder<R, T>>operator_doubleArrow(_streamHandlerBuilder, _function);
    StreamExtensions.<R, T>observe(stream, handler);
    handlerFn.apply(stream, handler);
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
  public static <R extends Object, T extends Object> void monitor(final IStream<R, T> stream, final Procedure1<? super StreamResponder> handlerFn) {
    final StreamResponderBuilder handler = new StreamResponderBuilder();
    handlerFn.apply(handler);
    StreamExtensions.<R, T>monitor(stream, handler);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> effect(final IStream<R, T> stream, final Procedure1<? super T> listener) {
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
    SubStream<R, T> _map = StreamExtensions.<R, T, T>map(stream, _function);
    final Procedure1<SubStream<R, T>> _function_1 = new Procedure1<SubStream<R, T>>() {
      public void apply(final SubStream<R, T> it) {
        stream.setOperation("effect");
      }
    };
    return ObjectExtensions.<SubStream<R, T>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * Opposite of collect, separate each list in the stream into separate
   * stream entries and streams those separately.
   */
  public static <R extends Object, T extends Object> SubStream<R, T> separate(final IStream<R, List<T>> stream) {
    SubStream<R, T> _xblockexpression = null;
    {
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, List<T>>> _function = new Procedure1<StreamHandlerBuilder<R, List<T>>>() {
        public void apply(final StreamHandlerBuilder<R, List<T>> it) {
          final Procedure2<R, List<T>> _function = new Procedure2<R, List<T>>() {
            public void apply(final R r, final List<T> list) {
              final Function1<T, Value<R, T>> _function = new Function1<T, Value<R, T>>() {
                public Value<R, T> apply(final T it) {
                  return new Value<R, T>(r, it);
                }
              };
              final List<Value<R, T>> entries = ListExtensions.<T, Value<R, T>>map(list, _function);
              Entries<R, T> _entries = new Entries<R, T>(((Entry<R, T>[])Conversions.unwrapArray(entries, Entry.class)));
              newStream.apply(_entries);
            }
          };
          it.each(_function);
          final Procedure2<R, Throwable> _function_1 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<R, Integer> _function_2 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
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
      StreamExtensions.<R, List<T>>on(stream, _function);
      stream.setOperation("separate");
      StreamExtensions.<R, R, List<T>, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Collect all items from a stream, separated by finishes
   */
  public static <R extends Object, T extends Object> SubStream<R, List<T>> collect(final IStream<R, T> stream) {
    SubStream<R, List<T>> _xblockexpression = null;
    {
      ArrayList<T> _newArrayList = CollectionLiterals.<T>newArrayList();
      final Function2<List<T>, T, List<T>> _function = new Function2<List<T>, T, List<T>>() {
        public List<T> apply(final List<T> list, final T it) {
          return StreamExtensions.<T>concat(list, it);
        }
      };
      final SubStream<R, List<T>> s = StreamExtensions.<R, T, List<T>>reduce(stream, _newArrayList, _function);
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
  public static <R extends Object, T extends Object> SubStream<R, String> join(final IStream<R, T> stream, final String separator) {
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
    SubStream<R, String> _reduce = StreamExtensions.<R, T, String>reduce(stream, "", _function);
    final Procedure1<SubStream<R, String>> _function_1 = new Procedure1<SubStream<R, String>>() {
      public void apply(final SubStream<R, String> it) {
        stream.setOperation("join");
      }
    };
    return ObjectExtensions.<SubStream<R, String>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Add the value of all the items in the stream until a finish.
   */
  public static <R extends Object, T extends Number> SubStream<R, Double> sum(final IStream<R, T> stream) {
    final Function2<Double, T, Double> _function = new Function2<Double, T, Double>() {
      public Double apply(final Double acc, final T it) {
        double _doubleValue = it.doubleValue();
        return Double.valueOf(((acc).doubleValue() + _doubleValue));
      }
    };
    SubStream<R, Double> _reduce = StreamExtensions.<R, T, Double>reduce(stream, Double.valueOf(0D), _function);
    final Procedure1<SubStream<R, Double>> _function_1 = new Procedure1<SubStream<R, Double>>() {
      public void apply(final SubStream<R, Double> it) {
        stream.setOperation("sum");
      }
    };
    return ObjectExtensions.<SubStream<R, Double>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Average the items in the stream until a finish.
   */
  public static <R extends Object, T extends Number> SubStream<R, Double> average(final IStream<R, T> stream) {
    SubStream<R, Pair<Integer, T>> _index = StreamExtensions.<R, T>index(stream);
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
    SubStream<R, Pair<Integer, Double>> _reduce = StreamExtensions.<R, Pair<Integer, T>, Pair<Integer, Double>>reduce(_index, _mappedTo, _function);
    final Function1<Pair<Integer, Double>, Double> _function_1 = new Function1<Pair<Integer, Double>, Double>() {
      public Double apply(final Pair<Integer, Double> it) {
        Double _value = it.getValue();
        Integer _key = it.getKey();
        return Double.valueOf(DoubleExtensions.operator_divide(_value, _key));
      }
    };
    SubStream<R, Double> _map = StreamExtensions.<R, Pair<Integer, Double>, Double>map(_reduce, _function_1);
    final Procedure1<SubStream<R, Double>> _function_2 = new Procedure1<SubStream<R, Double>>() {
      public void apply(final SubStream<R, Double> it) {
        stream.setOperation("average");
      }
    };
    return ObjectExtensions.<SubStream<R, Double>>operator_doubleArrow(_map, _function_2);
  }
  
  /**
   * Count the number of items passed in the stream until a finish.
   */
  public static <R extends Object, T extends Object> SubStream<R, Integer> count(final IStream<R, T> stream) {
    final Function2<Integer, T, Integer> _function = new Function2<Integer, T, Integer>() {
      public Integer apply(final Integer acc, final T it) {
        return Integer.valueOf(((acc).intValue() + 1));
      }
    };
    SubStream<R, Integer> _reduce = StreamExtensions.<R, T, Integer>reduce(stream, Integer.valueOf(0), _function);
    final Procedure1<SubStream<R, Integer>> _function_1 = new Procedure1<SubStream<R, Integer>>() {
      public void apply(final SubStream<R, Integer> it) {
        stream.setOperation("count");
      }
    };
    return ObjectExtensions.<SubStream<R, Integer>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Gives the maximum value found on the stream.
   * Values must implement Comparable
   */
  public static <R extends Object, T extends Comparable<T>> SubStream<R, T> max(final IStream<R, T> stream) {
    SubStream<R, T> _xblockexpression = null;
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
      final SubStream<R, T> s = StreamExtensions.<R, T, T>reduce(stream, null, _function);
      stream.setOperation("max");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Gives the minimum value found on the stream.
   * Values must implement Comparable
   */
  public static <R extends Object, T extends Comparable<T>> SubStream<R, T> min(final IStream<R, T> stream) {
    SubStream<R, T> _xblockexpression = null;
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
      final SubStream<R, T> s = StreamExtensions.<R, T, T>reduce(stream, null, _function);
      stream.setOperation("min");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * Errors in the stream are suppressed.
   */
  public static <R extends Object, T extends Object, P extends Object> SubStream<R, P> reduce(final IStream<R, T> stream, final P initial, final Function2<? super P, ? super T, ? extends P> reducerFn) {
    SubStream<R, P> _xblockexpression = null;
    {
      final AtomicReference<P> reduced = new AtomicReference<P>(initial);
      final SubStream<R, P> newStream = new SubStream<R, P>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              try {
                P _get = reduced.get();
                P _apply = reducerFn.apply(_get, $1);
                reduced.set(_apply);
              } finally {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                final P result = reduced.getAndSet(initial);
                boolean _notEquals = (!Objects.equal(result, null));
                if (_notEquals) {
                  newStream.push($0, result);
                } else {
                  Exception _exception = new Exception("no result found when reducing");
                  newStream.error($0, _exception);
                }
              } else {
                newStream.finish($0, (($1).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              reduced.set(initial);
              stream.skip();
              newStream.error($0, $1);
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation((("reduce(initial=" + initial) + ")"));
      StreamExtensions.<R, R, T, P>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <R extends Object, T extends Object, P extends Object> SubStream<R, P> scan(final IStream<R, T> stream, final P initial, final Function2<? super P, ? super T, ? extends P> reducerFn) {
    SubStream<R, P> _xblockexpression = null;
    {
      final AtomicReference<P> reduced = new AtomicReference<P>(initial);
      final SubStream<R, P> newStream = new SubStream<R, P>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              P _get = reduced.get();
              final P result = reducerFn.apply(_get, $1);
              reduced.set(result);
              boolean _notEquals = (!Objects.equal(result, null));
              if (_notEquals) {
                newStream.push($0, result);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              reduced.set(initial);
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation((("scan(initial=" + initial) + ")"));
      StreamExtensions.<R, R, T, P>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if all stream values match the test function
   */
  public static <R extends Object, T extends Object> SubStream<R, Boolean> all(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    SubStream<R, Boolean> _xblockexpression = null;
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
      final SubStream<R, Boolean> s = StreamExtensions.<R, T, Boolean>reduce(stream, Boolean.valueOf(true), _function);
      stream.setOperation("all");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if no stream values match the test function
   */
  public static <R extends Object, T extends Object> SubStream<R, Boolean> none(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    SubStream<R, Boolean> _xblockexpression = null;
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
      final SubStream<R, Boolean> s = StreamExtensions.<R, T, Boolean>reduce(stream, Boolean.valueOf(true), _function);
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
  public static <R extends Object, T extends Object> SubStream<R, Boolean> any(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    SubStream<R, Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final SubStream<R, Boolean> newStream = new SubStream<R, Boolean>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Boolean _apply = testFn.apply($1);
              if ((_apply).booleanValue()) {
                anyMatch.set(true);
                newStream.push($0, Boolean.valueOf(true));
                stream.skip();
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                final boolean matched = anyMatch.get();
                anyMatch.set(false);
                if ((!matched)) {
                  newStream.push($0, Boolean.valueOf(false));
                }
              } else {
                newStream.finish($0, (($1).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("any");
      StreamExtensions.<R, R, T, Boolean>controls(newStream, stream);
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
  public static <R extends Object, T extends Object> SubStream<R, T> first(final IStream<R, T> stream, final Function1<? super T, ? extends Boolean> testFn) {
    SubStream<R, T> _xblockexpression = null;
    {
      final AtomicReference<T> match = new AtomicReference<T>();
      final SubStream<R, T> newStream = new SubStream<R, T>(stream);
      final Procedure1<StreamHandlerBuilder<R, T>> _function = new Procedure1<StreamHandlerBuilder<R, T>>() {
        public void apply(final StreamHandlerBuilder<R, T> it) {
          final Procedure2<R, T> _function = new Procedure2<R, T>() {
            public void apply(final R $0, final T $1) {
              Boolean _apply = testFn.apply($1);
              if ((_apply).booleanValue()) {
                match.set($1);
                newStream.push($0, $1);
                stream.skip();
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<R, Integer> _function_1 = new Procedure2<R, Integer>() {
            public void apply(final R $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                match.set(null);
              } else {
                newStream.finish($0, (($1).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<R, Throwable> _function_2 = new Procedure2<R, Throwable>() {
            public void apply(final R $0, final Throwable $1) {
              newStream.error($0, $1);
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
      StreamExtensions.<R, T>on(stream, _function);
      stream.setOperation("first");
      StreamExtensions.<R, R, T, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Complete a task when the stream finishes or closes,
   * or give an error on the task when the stream gives an error.
   */
  public static <T extends Object> void pipe(final IStream<?, ?> stream, final Task task) {
    final Procedure1<StreamHandlerBuilder<?, ?>> _function = new Procedure1<StreamHandlerBuilder<?, ?>>() {
      public void apply(final StreamHandlerBuilder<?, ?> it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          public void apply(final Void it) {
            task.complete();
          }
        };
        it.closed(_function);
        final Procedure2<Object, Integer> _function_1 = new Procedure2<Object, Integer>() {
          public void apply(final Object $0, final Integer $1) {
            stream.close();
            task.complete();
          }
        };
        it.finish(_function_1);
        final Procedure2<Object, Throwable> _function_2 = new Procedure2<Object, Throwable>() {
          public void apply(final Object $0, final Throwable $1) {
            stream.close();
            task.error($1);
          }
        };
        it.error(_function_2);
        final Procedure2<Object, Object> _function_3 = new Procedure2<Object, Object>() {
          public void apply(final Object $0, final Object $1) {
          }
        };
        it.each(_function_3);
      }
    };
    StreamExtensions.on(stream, _function);
    stream.setOperation("toTask");
  }
  
  public static <T extends Object> Task toTask(final IStream<?, ?> stream) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      StreamExtensions.<Object>pipe(stream, task);
      _xblockexpression = task;
    }
    return _xblockexpression;
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
}
