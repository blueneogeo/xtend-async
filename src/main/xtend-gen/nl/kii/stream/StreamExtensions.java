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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import nl.kii.observe.Observable;
import nl.kii.observe.Publisher;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.promise.internal.SubTask;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamMonitor;
import nl.kii.stream.StreamStats;
import nl.kii.stream.internal.BaseStream;
import nl.kii.stream.internal.StreamEventHandler;
import nl.kii.stream.internal.StreamEventResponder;
import nl.kii.stream.internal.StreamException;
import nl.kii.stream.internal.StreamObserver;
import nl.kii.stream.internal.StreamResponder;
import nl.kii.stream.internal.SubStream;
import nl.kii.stream.internal.UncaughtStreamException;
import nl.kii.stream.message.Close;
import nl.kii.stream.message.Closed;
import nl.kii.stream.message.Entries;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Finish;
import nl.kii.stream.message.Next;
import nl.kii.stream.message.Overflow;
import nl.kii.stream.message.Skip;
import nl.kii.stream.message.StreamEvent;
import nl.kii.stream.message.Value;
import nl.kii.stream.source.LoadBalancer;
import nl.kii.stream.source.StreamCopySplitter;
import nl.kii.stream.source.StreamSource;
import nl.kii.util.Period;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.DoubleExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Functions.Function3;
import org.eclipse.xtext.xbase.lib.Functions.Function4;
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
      @Override
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
  public static <R extends Object, T extends Object, T2 extends Iterable<T>> Stream<T> stream(final IPromise<R, T2> promise) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
        @Override
        public void apply(final Throwable it) {
          newStream.error(it);
        }
      };
      IPromise<R, T2> _on = PromiseExtensions.<R, T2>on(promise, Throwable.class, _function);
      final Procedure1<T2> _function_1 = new Procedure1<T2>() {
        @Override
        public void apply(final T2 it) {
          Stream<T> _stream = StreamExtensions.<T>stream(it);
          StreamExtensions.<T, T>pipe(_stream, newStream);
        }
      };
      _on.then(_function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * stream an list, ending with a finish. makes an immutable copy internally.
   */
  public static <T extends Object> Stream<T> streamList(final List<T> list) {
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
        @Override
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
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              pushNext.apply();
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              finished.set(true);
              stream.finish();
            }
          };
          it.skip(_function_1);
        }
      };
      StreamExtensions.<T, T>when(stream, _function_1);
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
          @Override
          public Object getResult() {
            Object _xblockexpression = null;
            {
              newStream.finish();
              _xblockexpression = null;
            }
            return _xblockexpression;
          }
          
          @Override
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
        final Procedure1<StreamEventResponder> _function = new Procedure1<StreamEventResponder>() {
          @Override
          public void apply(final StreamEventResponder it) {
            final Procedure1<Void> _function = new Procedure1<Void>() {
              @Override
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
              @Override
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
        StreamExtensions.<List<Byte>, List<Byte>>when(newStream, _function);
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
      final Procedure1<StreamEventResponder> _function = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
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
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.close(_function_2);
        }
      };
      StreamExtensions.<Integer, Integer>when(newStream, _function);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create from the stream a new stream of just the output type
   */
  public static <I extends Object, O extends Object> IStream<O, O> newStream(final IStream<I, O> stream) {
    final Function2<I, O, O> _function = new Function2<I, O, O>() {
      @Override
      public O apply(final I in, final O out) {
        return out;
      }
    };
    return StreamExtensions.<I, O, O>newStreamOf(stream, _function);
  }
  
  /**
   * Transform the stream input based on the existing input and output type.
   */
  public static <I extends Object, O extends Object, T extends Object> Stream<T> newStreamOf(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends T> mapFn) {
    Stream<T> _xblockexpression = null;
    {
      final Stream<T> newStream = new Stream<T>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              T _apply = mapFn.apply($0, $1);
              newStream.push(_apply);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish(($1).intValue());
              stream.next();
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($1);
              stream.next();
            }
          };
          it.error(_function_2);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<T, I, O, T>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Observe the entries coming off this stream using a StreamObserver.
   * Note that you can only have ONE stream observer for every stream!
   * If you want more than one observer, you can split the stream, or
   * use StreamExtensions.monitor, which does this for you.
   * <p>
   * If you are using Xtend, it is recommended to use the StreamExtensions.on [ ]
   * instead, for a more concise and elegant builder syntax.
   */
  public static <I extends Object, O extends Object> void observe(final IStream<I, O> stream, final StreamObserver<I, O> observer) {
    stream.setOperation("observe");
    final Procedure1<Entry<I, O>> _function = new Procedure1<Entry<I, O>>() {
      @Override
      public void apply(final Entry<I, O> entry) {
        final Entry<I, O> it = entry;
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Value) {
            _matched=true;
            observer.onValue(((Value<I, O>)it).from, ((Value<I, O>)it).value);
          }
        }
        if (!_matched) {
          if (it instanceof Finish) {
            _matched=true;
            observer.onFinish(((Finish<I, O>)it).from, ((Finish<I, O>)it).level);
          }
        }
        if (!_matched) {
          if (it instanceof nl.kii.stream.message.Error) {
            _matched=true;
            observer.onError(((nl.kii.stream.message.Error<I, O>)it).from, ((nl.kii.stream.message.Error<I, O>)it).error);
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
   * Listen to commands given to the stream.
   */
  public static <I extends Object, O extends Object> void handle(final IStream<I, O> stream, final StreamEventHandler controller) {
    final Procedure1<StreamEvent> _function = new Procedure1<StreamEvent>() {
      @Override
      public void apply(final StreamEvent notification) {
        final StreamEvent it = notification;
        boolean _matched = false;
        if (!_matched) {
          if (it instanceof Next) {
            _matched=true;
            controller.onNext();
          }
        }
        if (!_matched) {
          if (it instanceof Skip) {
            _matched=true;
            controller.onSkip();
          }
        }
        if (!_matched) {
          if (it instanceof Close) {
            _matched=true;
            controller.onClose();
          }
        }
        if (!_matched) {
          if (it instanceof Overflow) {
            _matched=true;
            controller.onOverflow(((Overflow)it).entry);
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
  public static <I extends Object, O extends Object> Publisher<O> publish(final IStream<I, O> stream) {
    Publisher<O> _xblockexpression = null;
    {
      final Publisher<O> publisher = new Publisher<O>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              publisher.apply($1);
              Boolean _publishing = publisher.getPublishing();
              if ((_publishing).booleanValue()) {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              publisher.setPublishing(Boolean.valueOf(false));
            }
          };
          it.closed(_function_1);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
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
        @Override
        public void apply(final T it) {
          newStream.push(it);
        }
      };
      final Procedure0 stopObserving = observable.onChange(_function);
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stopObserving.apply();
            }
          };
          it.close(_function);
        }
      };
      StreamExtensions.<T, T>when(newStream, _function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleGreaterThan(final I input, final IStream<I, O> stream) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.push(input);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a value to a stream
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final I input) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.push(input);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleGreaterThan(final List<I> input, final IStream<I, O> stream) {
    IStream<I, O> _xblockexpression = null;
    {
      final Consumer<I> _function = new Consumer<I>() {
        @Override
        public void accept(final I it) {
          stream.push(it);
        }
      };
      input.forEach(_function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add a list of values to a stream
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final List<I> value) {
    IStream<I, O> _xblockexpression = null;
    {
      final Consumer<I> _function = new Consumer<I>() {
        @Override
        public void accept(final I it) {
          stream.push(it);
        }
      };
      value.forEach(_function);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Add an entry to a stream (such as error or finish)
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final Entry<I, O> entry) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      _input.apply(entry);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the << operator
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleLessThan(final IStream<I, O> stream, final Throwable t) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      nl.kii.stream.message.Error<I, O> _error = new nl.kii.stream.message.Error<I, O>(null, t);
      _input.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you easily pass an Error<T> to the stream using the >> operator
   */
  public static <I extends Object, O extends Object> IStream<I, O> operator_doubleGreaterThan(final Throwable t, final IStream<I, O> stream) {
    IStream<I, O> _xblockexpression = null;
    {
      IStream<I, I> _input = stream.getInput();
      nl.kii.stream.message.Error<I, O> _error = new nl.kii.stream.message.Error<I, O>(null, t);
      _input.apply(_error);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <I extends Object, O extends Object> StreamSource<I, O> operator_doubleGreaterThan(final IStream<I, O> source, final IStream<I, O> dest) {
    return StreamExtensions.<I, O>pipe(source, dest);
  }
  
  /**
   * pipe a stream into another stream
   */
  public static <I extends Object, O extends Object> StreamSource<I, O> operator_doubleLessThan(final IStream<I, O> dest, final IStream<I, O> source) {
    return StreamExtensions.<I, O>pipe(source, dest);
  }
  
  /**
   * split a source into a new destination stream
   */
  public static <I extends Object, O extends Object> StreamSource<I, O> operator_doubleGreaterThan(final StreamSource<I, O> source, final IStream<I, O> dest) {
    return source.pipe(dest);
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <I extends Object, O extends Object> Finish<I, O> finish() {
    return new Finish<I, O>(null, 0);
  }
  
  /**
   * Lets you easily pass a Finish<T> entry using the << or >> operators
   */
  public static <I extends Object, O extends Object> Finish<I, O> finish(final int level) {
    return new Finish<I, O>(null, level);
  }
  
  /**
   * Forwards commands given to the newStream directly to the parent.
   */
  public static <I1 extends Object, I2 extends Object, O1 extends Object, O2 extends Object> IStream<I1, O2> controls(final IStream<I1, O2> newStream, final IStream<I2, O1> parent) {
    final Procedure1<StreamEventResponder> _function = new Procedure1<StreamEventResponder>() {
      @Override
      public void apply(final StreamEventResponder it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          @Override
          public void apply(final Void it) {
            parent.next();
          }
        };
        it.next(_function);
        final Procedure1<Void> _function_1 = new Procedure1<Void>() {
          @Override
          public void apply(final Void it) {
            parent.skip();
          }
        };
        it.skip(_function_1);
        final Procedure1<Void> _function_2 = new Procedure1<Void>() {
          @Override
          public void apply(final Void it) {
            parent.close();
          }
        };
        it.close(_function_2);
      }
    };
    return StreamExtensions.<I1, O2>when(newStream, _function);
  }
  
  /**
   * Tell the stream something went wrong
   */
  public static <I extends Object, O extends Object> void error(final IStream<I, O> stream, final String message) {
    Exception _exception = new Exception(message);
    stream.error(_exception);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends Object, O extends Object> void error(final IStream<I, O> stream, final String message, final Object value) {
    StreamException _streamException = new StreamException(message, value, null);
    stream.error(_streamException);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends Object, O extends Object> void error(final IStream<I, O> stream, final String message, final Throwable cause) {
    Exception _exception = new Exception(message, cause);
    stream.error(_exception);
  }
  
  /**
   * Tell the stream something went wrong, with the cause throwable
   */
  public static <I extends Object, O extends Object> void error(final IStream<I, O> stream, final String message, final Object value, final Throwable cause) {
    StreamException _streamException = new StreamException(message, value, cause);
    stream.error(_streamException);
  }
  
  /**
   * Set the concurrency of the stream, letting you keep chaining by returning the stream.
   */
  public static <I extends Object, O extends Object> IStream<I, O> concurrency(final IStream<I, O> stream, final int value) {
    IStream<I, O> _xblockexpression = null;
    {
      stream.setConcurrency(Integer.valueOf(value));
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn
   */
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> map(final IStream<I, O> stream, final Function1<? super O, ? extends R> mappingFn) {
    final Function2<I, O, R> _function = new Function2<I, O, R>() {
      @Override
      public R apply(final I r, final O it) {
        return mappingFn.apply(it);
      }
    };
    return StreamExtensions.<I, O, R>map(stream, _function);
  }
  
  /**
   * Transform each item in the stream using the passed mappingFn.
   */
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> map(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends R> mappingFn) {
    SubStream<I, R> _xblockexpression = null;
    {
      final SubStream<I, R> newStream = new SubStream<I, R>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              final R mapped = mappingFn.apply($0, $1);
              newStream.push($0, mapped);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, R>controls(newStream, stream);
      stream.setOperation("map");
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> filter(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> filterFn) {
    final Function3<O, Long, Long, Boolean> _function = new Function3<O, Long, Long, Boolean>() {
      @Override
      public Boolean apply(final O it, final Long index, final Long passed) {
        return filterFn.apply(it);
      }
    };
    return StreamExtensions.<I, O>filter(stream, _function);
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> filter(final IStream<I, O> stream, final Function3<? super O, ? super Long, ? super Long, ? extends Boolean> filterFn) {
    final Function4<I, O, Long, Long, Boolean> _function = new Function4<I, O, Long, Long, Boolean>() {
      @Override
      public Boolean apply(final I r, final O it, final Long index, final Long passed) {
        return filterFn.apply(it, index, passed);
      }
    };
    return StreamExtensions.<I, O>filter(stream, _function);
  }
  
  /**
   * Filter items in a stream to only the ones that the filterFn
   * returns a true for. This version also counts the number of
   * items passed into the stream (the index) and the number of
   * items passed by this filter so far. Both of these numbers
   * are reset by a finish.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> filter(final IStream<I, O> stream, final Function4<? super I, ? super O, ? super Long, ? super Long, ? extends Boolean> filterFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              final long i = index.incrementAndGet();
              long _get = passed.get();
              Boolean _apply = filterFn.apply($0, $1, Long.valueOf(i), Long.valueOf(_get));
              if ((_apply).booleanValue()) {
                passed.incrementAndGet();
                newStream.push($0, $1);
              } else {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              index.set(0);
              passed.set(0);
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      stream.setOperation("filter");
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Splits a stream into multiple parts. These parts are separated by Finish entries.
   * <p>
   * Streams support multiple levels of finishes, to indicate multiple levels of splits.
   * This allows you to split a stream, and then split it again. Rules for splitting a stream:
   * <ul>
   * <li>when a new split is applied, it is always at finish level 0
   * <li>all other stream operations that use finish, always use this level
   * <li>the existing splits are all upgraded a level. so a level 0 finish becomes a level 1 finish
   * <li>splits at a higher level always are carried through to a lower level. so wherever there is a
   *     level 1 split for example, there is also a level 0 split
   * </ul>
   */
  public static <I extends Object, O extends Object> SubStream<I, O> split(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> splitConditionFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final AtomicBoolean skipping = new AtomicBoolean(false);
      final AtomicBoolean justPostedFinish0 = new AtomicBoolean(false);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              boolean _get = skipping.get();
              if (_get) {
                stream.next();
              } else {
                Boolean _apply = splitConditionFn.apply($1);
                if ((_apply).booleanValue()) {
                  Value<I, O> _value = new Value<I, O>($0, $1);
                  Finish<I, O> _finish = new Finish<I, O>($0, 0);
                  final List<? extends Entry<I, O>> entries = Collections.<Entry<I, O>>unmodifiableList(CollectionLiterals.<Entry<I, O>>newArrayList(_value, _finish));
                  justPostedFinish0.set(true);
                  Entries<I, O> _entries = new Entries<I, O>(((Entry<I, O>[])Conversions.unwrapArray(entries, Entry.class)));
                  newStream.apply(_entries);
                } else {
                  justPostedFinish0.set(false);
                  Value<I, O> _value_1 = new Value<I, O>($0, $1);
                  newStream.apply(_value_1);
                }
              }
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                skipping.set(false);
              }
              boolean _get = justPostedFinish0.get();
              if (_get) {
                Finish<I, Object> _finish = new Finish<I, Object>($0, (($1).intValue() + 1));
                newStream.apply(_finish);
              } else {
                justPostedFinish0.set(true);
                Finish<I, Object> _finish_1 = new Finish<I, Object>($0, 0);
                Finish<I, Object> _finish_2 = new Finish<I, Object>($0, (($1).intValue() + 1));
                final List<Finish<I, Object>> entries = Collections.<Finish<I, Object>>unmodifiableList(CollectionLiterals.<Finish<I, Object>>newArrayList(_finish_1, _finish_2));
                Entries<I, Object> _entries = new Entries<I, Object>(((Entry<I, Object>[])Conversions.unwrapArray(entries, Entry.class)));
                newStream.apply(_entries);
              }
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("split");
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.next();
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              skipping.set(true);
            }
          };
          it.skip(_function_2);
        }
      };
      StreamExtensions.<I, O>when(newStream, _function_1);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Merges one level of finishes.
   * @see StreamExtensions.split
   */
  public static <I extends Object, O extends Object> SubStream<I, O> merge(final IStream<I, O> stream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              Value<I, O> _value = new Value<I, O>($0, $1);
              newStream.apply(_value);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() > 0)) {
                newStream.finish($0, (($1).intValue() - 1));
              } else {
                stream.next();
              }
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("merge");
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Only let pass a certain amount of items through the stream
   */
  public static <I extends Object, O extends Object> SubStream<I, O> limit(final IStream<I, O> stream, final int amount) {
    final Function2<O, Long, Boolean> _function = new Function2<O, Long, Boolean>() {
      @Override
      public Boolean apply(final O it, final Long c) {
        return Boolean.valueOf(((c).longValue() > amount));
      }
    };
    SubStream<I, O> _until = StreamExtensions.<I, O>until(stream, _function);
    final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_until, _function_1);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> until(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> untilFn) {
    final Function3<O, Long, Long, Boolean> _function = new Function3<O, Long, Long, Boolean>() {
      @Override
      public Boolean apply(final O it, final Long index, final Long passed) {
        return untilFn.apply(it);
      }
    };
    return StreamExtensions.<I, O>until(stream, _function);
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * It is exclusive, meaning that if the value from the
   * stream matches the untilFn, that value will not be passed.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> until(final IStream<I, O> stream, final Function3<? super O, ? super Long, ? super Long, ? extends Boolean> untilFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final AtomicLong index = new AtomicLong(0);
      final AtomicLong passed = new AtomicLong(0);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
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
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              index.set(0);
              passed.set(0);
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Stream until the until condition Fn returns true.
   * Passes a counter as second parameter to the untilFn, starting at 1.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> until(final IStream<I, O> stream, final Function2<? super O, ? super Long, ? extends Boolean> untilFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final AtomicLong count = new AtomicLong(0);
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
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
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              count.set(0);
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("until");
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Flatten a stream of streams into a single stream.
   * <p>
   * Note: breaks finishes and flow control!
   */
  public static <I extends Object, I2 extends Object, O extends Object, S extends IStream<I2, O>> SubStream<I, O> flatten(final IStream<I, S> stream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, S>> _function = new Procedure1<StreamResponder<I, S>>() {
        @Override
        public void apply(final StreamResponder<I, S> it) {
          final Procedure2<I, S> _function = new Procedure2<I, S>() {
            @Override
            public void apply(final I r, final S s) {
              final Procedure1<StreamResponder<I2, O>> _function = new Procedure1<StreamResponder<I2, O>>() {
                @Override
                public void apply(final StreamResponder<I2, O> it) {
                  final Procedure2<I2, O> _function = new Procedure2<I2, O>() {
                    @Override
                    public void apply(final I2 $0, final O $1) {
                      newStream.push(r, $1);
                      s.next();
                    }
                  };
                  it.each(_function);
                  final Procedure2<I2, Throwable> _function_1 = new Procedure2<I2, Throwable>() {
                    @Override
                    public void apply(final I2 $0, final Throwable $1) {
                      newStream.error(r, $1);
                      s.next();
                    }
                  };
                  it.error(_function_1);
                }
              };
              StreamExtensions.<I2, O>on(s, _function);
              s.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_2);
        }
      };
      StreamExtensions.<I, S>on(stream, _function);
      stream.setOperation("flatten");
      StreamExtensions.<I, I, S, O>controls(newStream, stream);
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
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> flatMap(final IStream<I, O> stream, final Function1<? super O, ? extends IStream<I, R>> mapFn) {
    SubStream<I, IStream<I, R>> _map = StreamExtensions.<I, O, IStream<I, R>>map(stream, mapFn);
    SubStream<I, R> _flatten = StreamExtensions.<I, I, R, IStream<I, R>>flatten(_map);
    final Procedure1<SubStream<I, R>> _function = new Procedure1<SubStream<I, R>>() {
      @Override
      public void apply(final SubStream<I, R> it) {
        stream.setOperation("flatmap");
      }
    };
    return ObjectExtensions.<SubStream<I, R>>operator_doubleArrow(_flatten, _function);
  }
  
  /**
   * Keep count of how many items have been streamed so far, and for each
   * value from the original stream, push a pair of count->value.
   * Finish(0) resets the count.
   */
  public static <I extends Object, O extends Object> SubStream<I, Pair<Integer, O>> index(final IStream<I, O> stream) {
    SubStream<I, Pair<Integer, O>> _xblockexpression = null;
    {
      final SubStream<I, Pair<Integer, O>> newStream = new SubStream<I, Pair<Integer, O>>(stream);
      final AtomicInteger counter = new AtomicInteger(0);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              int _incrementAndGet = counter.incrementAndGet();
              Pair<Integer, O> _mappedTo = Pair.<Integer, O>of(Integer.valueOf(_incrementAndGet), $1);
              newStream.push($0, _mappedTo);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                counter.set(0);
              }
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("index");
      StreamExtensions.<I, I, O, Pair<Integer, O>>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Create a splitter StreamSource from a stream that lets you listen to the stream
   * with multiple listeners.
   */
  public static <I extends Object, O extends Object> StreamCopySplitter<I, O> split(final IStream<I, O> stream) {
    return new StreamCopySplitter<I, O>(stream);
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
  public static <I extends Object, O extends Object> SubStream<I, O> buffer(final IStream<I, O> stream, final int maxSize, final Procedure1<? super Entry<?, ?>> onOverflow) {
    SubStream<I, O> _xblockexpression = null;
    {
      boolean _matched = false;
      if (!_matched) {
        if (stream instanceof BaseStream) {
          _matched=true;
          ((BaseStream<I, O>)stream).setMaxBufferSize(Integer.valueOf(maxSize));
        }
      }
      final SubStream<I, O> newStream = new SubStream<I, O>(stream, maxSize);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.next();
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_2);
          final Procedure1<Entry<?, ?>> _function_3 = new Procedure1<Entry<?, ?>>() {
            @Override
            public void apply(final Entry<?, ?> it) {
              onOverflow.apply(it);
            }
          };
          it.overflow(_function_3);
        }
      };
      StreamExtensions.<I, O>when(newStream, _function_1);
      final Procedure1<StreamEventResponder> _function_2 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Entry<?, ?>> _function = new Procedure1<Entry<?, ?>>() {
            @Override
            public void apply(final Entry<?, ?> it) {
              onOverflow.apply(it);
            }
          };
          it.overflow(_function);
        }
      };
      StreamExtensions.<I, O>when(stream, _function_2);
      final Procedure1<SubStream<I, O>> _function_3 = new Procedure1<SubStream<I, O>>() {
        @Override
        public void apply(final SubStream<I, O> it) {
          String _operation = stream.getOperation();
          it.setOperation(_operation);
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(newStream, _function_3);
    }
    return _xblockexpression;
  }
  
  /**
   * Only allows one value for every timeInMs milliseconds. All other values are dropped.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> throttle(final IStream<I, O> stream, final long periodMs) {
    SubStream<I, O> _xblockexpression = null;
    {
      final AtomicLong startTime = new AtomicLong((-1));
      final Function1<O, Boolean> _function = new Function1<O, Boolean>() {
        @Override
        public Boolean apply(final O it) {
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
      SubStream<I, O> _filter = StreamExtensions.<I, O>filter(stream, _function);
      final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
        @Override
        public void apply(final SubStream<I, O> it) {
          stream.setOperation((("throttle(periodMs=" + Long.valueOf(periodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_filter, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Only allows one value per given period. Other values are dropped.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> throttle(final IStream<I, O> stream, final Period period) {
    long _ms = period.ms();
    return StreamExtensions.<I, O>throttle(stream, _ms);
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
   * FIX: BREAKS ON ERRORS
   */
  public static <I extends Object, O extends Object> IStream<I, O> ratelimit(final IStream<I, O> stream, final long periodMs, final Procedure2<? super Long, ? super Procedure0> timerFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      if ((periodMs <= 0)) {
        return stream;
      }
      final AtomicLong lastNextMs = new AtomicLong((-1));
      final AtomicBoolean isTiming = new AtomicBoolean();
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<Entry<I, O>> _function = new Procedure1<Entry<I, O>>() {
        @Override
        public void apply(final Entry<I, O> it) {
          newStream.apply(it);
        }
      };
      stream.onChange(_function);
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
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
                    @Override
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
            @Override
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_2);
        }
      };
      StreamExtensions.<I, O>when(newStream, _function_1);
      final Procedure1<SubStream<I, O>> _function_2 = new Procedure1<SubStream<I, O>>() {
        @Override
        public void apply(final SubStream<I, O> it) {
          stream.setOperation((("ratelimit(periodMs=" + Long.valueOf(periodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(newStream, _function_2);
    }
    return _xblockexpression;
  }
  
  /**
   * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
   * <p>
   * When an error occurs, delay the next call by a given period, and if the next stream value again generates
   * an error, multiply the period by 2 and wait that period. This way increasing the period
   * up to a maximum period of one hour per error. The moment a normal value gets processed, the period is reset
   * to the initial period.
   */
  public static <I extends Object, O extends Object> IStream<I, O> backoff(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final long periodMs, final Procedure2<? super Long, ? super Procedure0> timerFn) {
    IStream<I, O> _xblockexpression = null;
    {
      final int hourMs = ((60 * 60) * 1000);
      _xblockexpression = StreamExtensions.<I, O>backoff(stream, errorType, periodMs, 2, hourMs, timerFn);
    }
    return _xblockexpression;
  }
  
  /**
   * Incrementally slow down the stream on errors, and recovering to full speed once a normal value is processed.
   * <p>
   * When an error occurs, delay the next call by a given period, and if the next stream value again generates
   * an error, multiply the period by the given factor and wait that period. This way increasing the period
   * up to a maximum period that you pass. The moment a normal value gets processed, the period is reset to the
   * initial period.
   * 
   * TODO: needs testing!
   */
  public static <I extends Object, O extends Object> IStream<I, O> backoff(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final long periodMs, final int factor, final long maxPeriodMs, final Procedure2<? super Long, ? super Procedure0> timerFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      if (((periodMs <= 0) || (maxPeriodMs <= 0))) {
        return stream;
      }
      final AtomicLong delay = new AtomicLong(periodMs);
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
              delay.set(periodMs);
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_2);
          final Procedure2<I, Throwable> _function_3 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              long _get = delay.get();
              final Procedure0 _function = new Procedure0() {
                @Override
                public void apply() {
                  newStream.error($0, $1);
                }
              };
              timerFn.apply(Long.valueOf(_get), _function);
              long _get_1 = delay.get();
              long _multiply = (_get_1 * factor);
              final long newDelay = Math.min(_multiply, maxPeriodMs);
              delay.set(newDelay);
            }
          };
          it.error(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
        @Override
        public void apply(final SubStream<I, O> it) {
          stream.setOperation((((((("onErrorBackoff(periodMs=" + Long.valueOf(periodMs)) + ", factor=") + Integer.valueOf(factor)) + ", maxPeriodMs=") + Long.valueOf(maxPeriodMs)) + ")"));
        }
      };
      _xblockexpression = ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(newStream, _function_1);
    }
    return _xblockexpression;
  }
  
  /**
   * Splits a stream of values up into a stream of lists of those values,
   * where each new list is started at each period interval.
   * <p>
   * FIX: this can break the finishes, lists may come in after a finish.
   * FIX: somehow ratelimit and window in a single stream do not time well together
   */
  public static <I extends Object, O extends Object> SubStream<I, List<O>> window(final IStream<I, O> stream, final long periodMs, final Procedure2<? super Long, ? super Procedure0> timerFn) {
    SubStream<I, List<O>> _xblockexpression = null;
    {
      final SubStream<I, List<O>> newStream = new SubStream<I, List<O>>(stream);
      final AtomicReference<List<O>> list = new AtomicReference<List<O>>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              List<O> _get = list.get();
              boolean _equals = Objects.equal(_get, null);
              if (_equals) {
                LinkedList<O> _newLinkedList = CollectionLiterals.<O>newLinkedList();
                list.set(_newLinkedList);
                final Procedure0 _function = new Procedure0() {
                  @Override
                  public void apply() {
                    List<O> _andSet = list.getAndSet(null);
                    newStream.push($0, _andSet);
                  }
                };
                timerFn.apply(Long.valueOf(periodMs), _function);
              }
              List<O> _get_1 = list.get();
              if (_get_1!=null) {
                _get_1.add($1);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_2);
          final Procedure2<I, Throwable> _function_3 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, List<O>>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Push a value onto the stream from the parent stream every time the timerstream pushes a new value.
   * <p>
   * Errors on the timerstream are put onto the stream. Closing the timerstream also closes the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> synchronizeWith(final IStream<I, O> stream, final IStream<?, ?> timerStream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<?, ?>> _function = new Procedure1<StreamResponder<?, ?>>() {
        @Override
        public void apply(final StreamResponder<?, ?> it) {
          final Procedure2<Object, Throwable> _function = new Procedure2<Object, Throwable>() {
            @Override
            public void apply(final Object $0, final Throwable $1) {
              newStream.error(null, $1);
              timerStream.next();
            }
          };
          it.error(_function);
          final Procedure2<Object, Integer> _function_1 = new Procedure2<Object, Integer>() {
            @Override
            public void apply(final Object $0, final Integer $1) {
              newStream.finish();
              timerStream.next();
            }
          };
          it.finish(_function_1);
          final Procedure2<Object, Object> _function_2 = new Procedure2<Object, Object>() {
            @Override
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
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.on(timerStream, _function);
      final Procedure1<StreamResponder<I, O>> _function_1 = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function_1);
      final Procedure1<StreamEventResponder> _function_2 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_1);
        }
      };
      StreamExtensions.<I, O>when(newStream, _function_2);
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
  public static <I extends Object, I2 extends Object, O extends Object> SubStream<I, O> resolve(final IStream<I, ? extends IPromise<I2, O>> stream) {
    Integer _concurrency = stream.getConcurrency();
    SubStream<I, O> _resolve = StreamExtensions.<I, O>resolve(stream, (_concurrency).intValue());
    final Procedure1<SubStream<I, O>> _function = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation("resolve");
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_resolve, _function);
  }
  
  /**
   * Resolves a stream of processes, meaning it waits for promises to finish and return their
   * values, and builds a stream of that.
   * <p>
   * Allows concurrent promises to be resolved in parallel.
   * Passing a concurrency of 0 means all incoming promises will be called concurrently.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> resolve(final IStream<I, ? extends IPromise<?, O>> stream, final int concurrency) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final AtomicBoolean isFinished = new AtomicBoolean(false);
      final AtomicInteger processes = new AtomicInteger(0);
      final Procedure1<StreamResponder<I, ? extends IPromise<?, O>>> _function = new Procedure1<StreamResponder<I, ? extends IPromise<?, O>>>() {
        @Override
        public void apply(final StreamResponder<I, ? extends IPromise<?, O>> it) {
          final Procedure2<I, IPromise<?, O>> _function = new Procedure2<I, IPromise<?, O>>() {
            @Override
            public void apply(final I r, final IPromise<?, O> promise) {
              final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
                @Override
                public void apply(final Throwable it) {
                  StreamException _streamException = new StreamException("resolve", r, it);
                  newStream.error(r, _streamException);
                  boolean _and = false;
                  int _decrementAndGet = processes.decrementAndGet();
                  boolean _equals = (_decrementAndGet == 0);
                  if (!_equals) {
                    _and = false;
                  } else {
                    boolean _compareAndSet = isFinished.compareAndSet(true, false);
                    _and = _compareAndSet;
                  }
                  if (_and) {
                    newStream.finish(r);
                  }
                }
              };
              IPromise<?, O> _on = PromiseExtensions.on(promise, Throwable.class, _function);
              final Procedure1<O> _function_1 = new Procedure1<O>() {
                @Override
                public void apply(final O it) {
                  newStream.push(r, it);
                  boolean _and = false;
                  int _decrementAndGet = processes.decrementAndGet();
                  boolean _equals = (_decrementAndGet == 0);
                  if (!_equals) {
                    _and = false;
                  } else {
                    boolean _compareAndSet = isFinished.compareAndSet(true, false);
                    _and = _compareAndSet;
                  }
                  if (_and) {
                    newStream.finish(r);
                  }
                }
              };
              _on.then(_function_1);
              boolean _or = false;
              int _incrementAndGet = processes.incrementAndGet();
              boolean _greaterThan = (concurrency > _incrementAndGet);
              if (_greaterThan) {
                _or = true;
              } else {
                _or = (concurrency == 0);
              }
              if (_or) {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
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
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.on(stream, _function);
      final Procedure1<StreamEventResponder> _function_1 = new Procedure1<StreamEventResponder>() {
        @Override
        public void apply(final StreamEventResponder it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.next();
            }
          };
          it.next(_function);
          final Procedure1<Void> _function_1 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.skip();
            }
          };
          it.skip(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.close(_function_2);
        }
      };
      StreamExtensions.<I, O>when(newStream, _function_1);
      stream.setOperation((("resolve(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(stream.concurrency)
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubStream<I, R> call(final IStream<I, O> stream, final Function1<? super O, ? extends P> promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    SubStream<I, R> _call = StreamExtensions.<I, O, R, P>call(stream, (_concurrency).intValue(), promiseFn);
    final Procedure1<SubStream<I, R>> _function = new Procedure1<SubStream<I, R>>() {
      @Override
      public void apply(final SubStream<I, R> it) {
        stream.setOperation("call");
      }
    };
    return ObjectExtensions.<SubStream<I, R>>operator_doubleArrow(_call, _function);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.call(stream.concurrency)
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubStream<I, R> call(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends P> promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    SubStream<I, R> _call = StreamExtensions.<I, O, R, P>call(stream, (_concurrency).intValue(), promiseFn);
    final Procedure1<SubStream<I, R>> _function = new Procedure1<SubStream<I, R>>() {
      @Override
      public void apply(final SubStream<I, R> it) {
        stream.setOperation("call");
      }
    };
    return ObjectExtensions.<SubStream<I, R>>operator_doubleArrow(_call, _function);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubStream<I, R> call(final IStream<I, O> stream, final int concurrency, final Function1<? super O, ? extends P> promiseFn) {
    final Function2<I, O, P> _function = new Function2<I, O, P>() {
      @Override
      public P apply(final I i, final O o) {
        return promiseFn.apply(o);
      }
    };
    return StreamExtensions.<I, O, R, P>call(stream, concurrency, _function);
  }
  
  /**
   * Make an asynchronous call.
   * This is an alias for stream.map(mappingFn).resolve(concurrency)
   */
  public static <I extends Object, O extends Object, R extends Object, P extends IPromise<?, R>> SubStream<I, R> call(final IStream<I, O> stream, final int concurrency, final Function2<? super I, ? super O, ? extends P> promiseFn) {
    SubStream<I, P> _map = StreamExtensions.<I, O, P>map(stream, promiseFn);
    SubStream<I, R> _resolve = StreamExtensions.<I, R>resolve(_map, concurrency);
    final Procedure1<SubStream<I, R>> _function = new Procedure1<SubStream<I, R>>() {
      @Override
      public void apply(final SubStream<I, R> it) {
        stream.setOperation((("call(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, R>>operator_doubleArrow(_resolve, _function);
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> SubStream<I, O> onError(final IStream<I, O> stream, final Procedure1<? super Throwable> handler) {
    return StreamExtensions.<I, O>on(stream, Throwable.class, handler);
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> SubStream<I, O> onError(final IStream<I, O> stream, final Procedure2<? super I, ? super Throwable> handler) {
    return StreamExtensions.<I, O>on(stream, Throwable.class, handler);
  }
  
  /**
   * Catch errors of the specified type coming from the stream, and call the handler with the error.
   * If swallow is true, the error will be caught and not be passed on (much like you expect a normal Java catch to work).
   */
  public static <I extends Object, O extends Object> SubStream<I, O> on(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final boolean swallow, final Procedure2<? super I, ? super Throwable> handler) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I from, final Throwable err) {
              try {
                Class<? extends Throwable> _class = err.getClass();
                boolean _isAssignableFrom = errorType.isAssignableFrom(_class);
                if (_isAssignableFrom) {
                  handler.apply(from, err);
                  if ((!swallow)) {
                    newStream.error(from, err);
                  }
                } else {
                  newStream.error(from, err);
                }
              } catch (final Throwable _t) {
                if (_t instanceof Throwable) {
                  final Throwable t = (Throwable)_t;
                  newStream.error(from, t);
                } else {
                  throw Exceptions.sneakyThrow(_t);
                }
              } finally {
                stream.next();
              }
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Catch errors of the specified type, call the handler, and swallow them from the stream chain.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> on(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Procedure2<? super I, ? super Throwable> handler) {
    final Procedure2<I, Throwable> _function = new Procedure2<I, Throwable>() {
      @Override
      public void apply(final I $0, final Throwable $1) {
        handler.apply($0, $1);
      }
    };
    return StreamExtensions.<I, O>on(stream, errorType, true, _function);
  }
  
  /**
   * Catch errors of the specified type, call the handler, and swallow them from the stream chain.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> on(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Procedure1<? super Throwable> handler) {
    final Procedure2<I, Throwable> _function = new Procedure2<I, Throwable>() {
      @Override
      public void apply(final I $0, final Throwable $1) {
        handler.apply($1);
      }
    };
    return StreamExtensions.<I, O>on(stream, errorType, true, _function);
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> map(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Function1<? super Throwable, ? extends O> mappingFn) {
    final Function2<I, Throwable, O> _function = new Function2<I, Throwable, O>() {
      @Override
      public O apply(final I input, final Throwable err) {
        return mappingFn.apply(err);
      }
    };
    return StreamExtensions.<I, O>map(stream, errorType, _function);
  }
  
  /**
   * Map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> map(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Function2<? super I, ? super Throwable, ? extends O> mappingFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I from, final Throwable err) {
              try {
                Class<? extends Throwable> _class = err.getClass();
                boolean _isAssignableFrom = errorType.isAssignableFrom(_class);
                if (_isAssignableFrom) {
                  final O value = mappingFn.apply(from, err);
                  newStream.push(from, value);
                } else {
                  newStream.error(from, err);
                }
              } catch (final Throwable _t) {
                if (_t instanceof Throwable) {
                  final Throwable t = (Throwable)_t;
                  newStream.error(from, t);
                } else {
                  throw Exceptions.sneakyThrow(_t);
                }
              }
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> call(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Function1<? super Throwable, ? extends IPromise<?, O>> mappingFn) {
    final Function2<I, Throwable, IPromise<?, O>> _function = new Function2<I, Throwable, IPromise<?, O>>() {
      @Override
      public IPromise<?, O> apply(final I input, final Throwable err) {
        return mappingFn.apply(err);
      }
    };
    return StreamExtensions.<I, O>call(stream, errorType, _function);
  }
  
  /**
   * Asynchronously map an error back to a value. Swallows the error.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> call(final IStream<I, O> stream, final Class<? extends Throwable> errorType, final Function2<? super I, ? super Throwable, ? extends IPromise<?, O>> mappingFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I from, final Throwable err) {
              try {
                Class<? extends Throwable> _class = err.getClass();
                boolean _isAssignableFrom = errorType.isAssignableFrom(_class);
                if (_isAssignableFrom) {
                  IPromise<?, O> _apply = mappingFn.apply(from, err);
                  final Procedure1<O> _function = new Procedure1<O>() {
                    @Override
                    public void apply(final O it) {
                      newStream.push(from, it);
                    }
                  };
                  Task _then = _apply.then(_function);
                  final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
                    @Override
                    public void apply(final Throwable it) {
                      newStream.error(from, it);
                    }
                  };
                  PromiseExtensions.<Boolean, Boolean>on(_then, Throwable.class, _function_1);
                } else {
                  newStream.error(from, err);
                }
              } catch (final Throwable _t) {
                if (_t instanceof Throwable) {
                  final Throwable t = (Throwable)_t;
                  newStream.error(from, t);
                } else {
                  throw Exceptions.sneakyThrow(_t);
                }
              }
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> SubStream<I, O> onErrorThrow(final IStream<I, O> stream) {
    return StreamExtensions.<I, O>onErrorThrow(stream, "onErrorThrow");
  }
  
  @Deprecated
  public static <I extends Object, O extends Object> SubStream<I, O> onErrorThrow(final IStream<I, O> stream, final String message) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              try {
                throw new UncaughtStreamException(message, $0, $1);
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Lets you respond to the closing of the stream
   */
  public static <I extends Object, O extends Object> SubStream<I, O> onClosed(final IStream<I, O> stream, final Procedure1<? super Void> handler) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
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
      StreamExtensions.<I, O>on(stream, _function);
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends Object, O extends Object> SubTask<I> onEach(final IStream<I, O> stream, final Procedure1<? super O> handler) {
    final Procedure2<I, O> _function = new Procedure2<I, O>() {
      @Override
      public void apply(final I r, final O it) {
        handler.apply(it);
      }
    };
    return StreamExtensions.<I, O>onEach(stream, _function);
  }
  
  /**
   * Synchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends Object, O extends Object> SubTask<I> onEach(final IStream<I, O> stream, final Procedure2<? super I, ? super O> handler) {
    SubTask<I> _xblockexpression = null;
    {
      final SubTask<I> task = new SubTask<I>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              handler.apply($0, $1);
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              task.error($0, $1);
              stream.next();
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                task.set($0, Boolean.valueOf(true));
              }
              stream.next();
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              stream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
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
  public static <I extends Object, I2 extends Object, O extends Object, R extends Object, P extends IPromise<I2, R>> SubTask<I> onEachCall(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends P> taskFn) {
    SubStream<I, P> _map = StreamExtensions.<I, O, P>map(stream, taskFn);
    SubStream<I, R> _resolve = StreamExtensions.<I, I2, R>resolve(_map);
    final Procedure1<R> _function = new Procedure1<R>() {
      @Override
      public void apply(final R it) {
      }
    };
    SubTask<I> _onEach = StreamExtensions.<I, R>onEach(_resolve, _function);
    final Procedure1<SubTask<I>> _function_1 = new Procedure1<SubTask<I>>() {
      @Override
      public void apply(final SubTask<I> it) {
        stream.setOperation("onEachCall");
      }
    };
    return ObjectExtensions.<SubTask<I>>operator_doubleArrow(_onEach, _function_1);
  }
  
  /**
   * Asynchronous listener to the stream, that automatically requests the next value after each value is handled.
   * Performs the task for every value, and only requests the next value from the stream once the task has finished.
   * Returns a task that completes once the stream finishes or closes.
   */
  public static <I extends Object, I2 extends Object, O extends Object, R extends Object, P extends IPromise<I2, R>> SubTask<I> onEachCall(final IStream<I, O> stream, final Function1<? super O, ? extends P> taskFn) {
    SubStream<I, P> _map = StreamExtensions.<I, O, P>map(stream, taskFn);
    SubStream<I, R> _resolve = StreamExtensions.<I, I2, R>resolve(_map);
    final Procedure1<R> _function = new Procedure1<R>() {
      @Override
      public void apply(final R it) {
      }
    };
    SubTask<I> _onEach = StreamExtensions.<I, R>onEach(_resolve, _function);
    final Procedure1<SubTask<I>> _function_1 = new Procedure1<SubTask<I>>() {
      @Override
      public void apply(final SubTask<I> it) {
        stream.setOperation("onEachCall");
      }
    };
    return ObjectExtensions.<SubTask<I>>operator_doubleArrow(_onEach, _function_1);
  }
  
  /**
   * Shortcut for splitting a stream and then performing a pipe to another stream.
   * @return a CopySplitter source that you can connect more streams to.
   */
  public static <I extends Object, O extends Object> StreamSource<I, O> pipe(final IStream<I, O> stream, final IStream<I, ?> target) {
    StreamCopySplitter<I, O> _split = StreamExtensions.<I, O>split(stream);
    StreamSource<I, O> _pipe = _split.pipe(target);
    final Procedure1<StreamSource<I, O>> _function = new Procedure1<StreamSource<I, O>>() {
      @Override
      public void apply(final StreamSource<I, O> it) {
        stream.setOperation("pipe");
      }
    };
    return ObjectExtensions.<StreamSource<I, O>>operator_doubleArrow(_pipe, _function);
  }
  
  /**
   * Start the stream and promise the first value coming from the stream.
   * Closes the stream once it has the value or an error.
   */
  public static <I extends Object, O extends Object> IPromise<I, O> first(final IStream<I, O> stream) {
    SubPromise<I, O> _xblockexpression = null;
    {
      Promise<I> _promise = new Promise<I>();
      final SubPromise<I, O> promise = new SubPromise<I, O>(_promise);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.set($0, $1);
              }
              stream.close();
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                promise.error($0, $1);
              }
              stream.close();
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              PromiseExtensions.<I, O>error(promise, "Stream.first: stream finished without returning a value");
              stream.close();
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              PromiseExtensions.<I, O>error(promise, "Stream.first: stream closed without returning a value");
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
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
  public static <I extends Object, O extends Object> SubPromise<I, O> last(final IStream<I, O> stream) {
    SubPromise<I, O> _xblockexpression = null;
    {
      Promise<I> _promise = new Promise<I>();
      final SubPromise<I, O> promise = new SubPromise<I, O>(_promise);
      final AtomicReference<O> last = new AtomicReference<O>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (_not) {
                last.set($1);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                boolean _and = false;
                Boolean _fulfilled = promise.getFulfilled();
                boolean _not = (!(_fulfilled).booleanValue());
                if (!_not) {
                  _and = false;
                } else {
                  O _get = last.get();
                  boolean _notEquals = (!Objects.equal(_get, null));
                  _and = _notEquals;
                }
                if (_and) {
                  O _get_1 = last.get();
                  promise.set($0, _get_1);
                  stream.close();
                } else {
                  PromiseExtensions.<I, O>error(promise, "stream finished without passing a value, no last entry found.");
                }
              } else {
                stream.next();
              }
            }
          };
          it.finish(_function_1);
          final Procedure1<Void> _function_2 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              boolean _and = false;
              Boolean _fulfilled = promise.getFulfilled();
              boolean _not = (!(_fulfilled).booleanValue());
              if (!_not) {
                _and = false;
              } else {
                O _get = last.get();
                boolean _notEquals = (!Objects.equal(_get, null));
                _and = _notEquals;
              }
              if (_and) {
                O _get_1 = last.get();
                promise.set(null, _get_1);
                stream.close();
              } else {
                PromiseExtensions.<I, O>error(promise, "stream closed without passing a value, no last entry found.");
              }
            }
          };
          it.closed(_function_2);
          final Procedure2<I, Throwable> _function_3 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              stream.next();
            }
          };
          it.error(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
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
  public static <I extends Object, O extends Object> SubStream<I, O> skip(final IStream<I, O> stream, final int amount) {
    final Function3<O, Long, Long, Boolean> _function = new Function3<O, Long, Long, Boolean>() {
      @Override
      public Boolean apply(final O it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    SubStream<I, O> _filter = StreamExtensions.<I, O>filter(stream, _function);
    final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation((("skip(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_filter, _function_1);
  }
  
  /**
   * Take only a set amount of items from the stream.
   * Resets at finish.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> take(final IStream<I, O> stream, final int amount) {
    final Function3<O, Long, Long, Boolean> _function = new Function3<O, Long, Long, Boolean>() {
      @Override
      public Boolean apply(final O it, final Long index, final Long passed) {
        return Boolean.valueOf(((index).longValue() > amount));
      }
    };
    SubStream<I, O> _until = StreamExtensions.<I, O>until(stream, _function);
    final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation((("limit(amount=" + Integer.valueOf(amount)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_until, _function_1);
  }
  
  /**
   * Start the stream and and promise the first value from it.
   * TODO: Task<R>
   */
  public static <I extends Object, O extends Object> Task then(final IStream<I, O> stream, final Procedure1<O> listener) {
    IPromise<I, O> _first = StreamExtensions.<I, O>first(stream);
    Task _then = _first.then(listener);
    final Procedure1<Task> _function = new Procedure1<Task>() {
      @Override
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
  public static <I extends Object, O extends Object> IStream<I, O> on(final IStream<I, O> stream, final Procedure1<? super StreamResponder<I, O>> handlerFn) {
    final Procedure2<IStream<I, O>, StreamResponder<I, O>> _function = new Procedure2<IStream<I, O>, StreamResponder<I, O>>() {
      @Override
      public void apply(final IStream<I, O> s, final StreamResponder<I, O> builder) {
        handlerFn.apply(builder);
      }
    };
    return StreamExtensions.<I, O>on(stream, _function);
  }
  
  public static <I extends Object, O extends Object> IStream<I, O> on(final IStream<I, O> stream, final Procedure2<? super IStream<I, O>, ? super StreamResponder<I, O>> handlerFn) {
    IStream<I, O> _xblockexpression = null;
    {
      StreamResponder<I, O> _streamResponder = new StreamResponder<I, O>();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          it.setStream(stream);
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              stream.next();
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              stream.next();
            }
          };
          it.error(_function_2);
        }
      };
      final StreamResponder<I, O> handler = ObjectExtensions.<StreamResponder<I, O>>operator_doubleArrow(_streamResponder, _function);
      StreamExtensions.<I, O>observe(stream, handler);
      handlerFn.apply(stream, handler);
      _xblockexpression = stream;
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
   * stream.when [
   *     next [ println('next was called on the stream') ]
   *     close [ println('the stream was closed') ]
   * ]
   * </pre>
   */
  public static <I extends Object, O extends Object> IStream<I, O> when(final IStream<I, O> stream, final Procedure1<? super StreamEventResponder> handlerFn) {
    IStream<I, O> _xblockexpression = null;
    {
      final StreamEventResponder handler = new StreamEventResponder();
      handlerFn.apply(handler);
      StreamExtensions.<I, O>handle(stream, handler);
      _xblockexpression = stream;
    }
    return _xblockexpression;
  }
  
  /**
   * Check on each value if the assert/check description is valid.
   * Throws an Exception with the check description if not.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> check(final IStream<I, O> stream, final String checkDescription, final Function1<? super O, ? extends Boolean> checkFn) {
    final Procedure1<O> _function = new Procedure1<O>() {
      @Override
      public void apply(final O it) {
        try {
          Boolean _apply = checkFn.apply(it);
          boolean _not = (!(_apply).booleanValue());
          if (_not) {
            throw new Exception(((checkDescription + "- for value ") + it));
          }
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    return StreamExtensions.<I, O>effect(stream, _function);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> effect(final IStream<I, O> stream, final Procedure1<? super O> listener) {
    final Procedure2<I, O> _function = new Procedure2<I, O>() {
      @Override
      public void apply(final I r, final O it) {
        listener.apply(it);
      }
    };
    return StreamExtensions.<I, O>effect(stream, _function);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> effect(final IStream<I, O> stream, final Procedure2<? super I, ? super O> listener) {
    final Function2<I, O, O> _function = new Function2<I, O, O>() {
      @Override
      public O apply(final I r, final O it) {
        O _xblockexpression = null;
        {
          listener.apply(r, it);
          _xblockexpression = it;
        }
        return _xblockexpression;
      }
    };
    SubStream<I, O> _map = StreamExtensions.<I, O, O>map(stream, _function);
    final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation("effect");
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> perform(final IStream<I, O> stream, final Function1<? super O, ? extends IPromise<?, ?>> promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    return StreamExtensions.<I, O>perform(stream, (_concurrency).intValue(), promiseFn);
  }
  
  /**
   * Perform some side-effect action based on the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> perform(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends IPromise<?, ?>> promiseFn) {
    Integer _concurrency = stream.getConcurrency();
    return StreamExtensions.<I, O>perform(stream, (_concurrency).intValue(), promiseFn);
  }
  
  /**
   * Perform some asynchronous side-effect action based on the stream.
   * Perform at most 'concurrency' calls in parallel.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> perform(final IStream<I, O> stream, final int concurrency, final Function1<? super O, ? extends IPromise<?, ?>> promiseFn) {
    final Function2<I, O, IPromise<?, ?>> _function = new Function2<I, O, IPromise<?, ?>>() {
      @Override
      public IPromise<?, ?> apply(final I i, final O o) {
        return promiseFn.apply(o);
      }
    };
    return StreamExtensions.<I, O>perform(stream, concurrency, _function);
  }
  
  /**
   * Perform some asynchronous side-effect action based on the stream.
   * Perform at most 'concurrency' calls in parallel.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> perform(final IStream<I, O> stream, final int concurrency, final Function2<? super I, ? super O, ? extends IPromise<?, ?>> promiseFn) {
    final Function2<I, O, SubPromise<?, O>> _function = new Function2<I, O, SubPromise<?, O>>() {
      @Override
      public SubPromise<?, O> apply(final I i, final O o) {
        IPromise<?, ?> _apply = promiseFn.apply(i, o);
        final Function1<Object, O> _function = new Function1<Object, O>() {
          @Override
          public O apply(final Object it) {
            return o;
          }
        };
        return PromiseExtensions.map(_apply, _function);
      }
    };
    SubStream<I, O> _call = StreamExtensions.<I, O, O, SubPromise<?, O>>call(stream, concurrency, _function);
    final Procedure1<SubStream<I, O>> _function_1 = new Procedure1<SubStream<I, O>>() {
      @Override
      public void apply(final SubStream<I, O> it) {
        stream.setOperation((("perform(concurrency=" + Integer.valueOf(concurrency)) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, O>>operator_doubleArrow(_call, _function_1);
  }
  
  /**
   * Opposite of collect, separate each list in the stream into separate
   * stream entries and streams those separately.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> separate(final IStream<I, List<O>> stream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, List<O>>> _function = new Procedure1<StreamResponder<I, List<O>>>() {
        @Override
        public void apply(final StreamResponder<I, List<O>> it) {
          final Procedure2<I, List<O>> _function = new Procedure2<I, List<O>>() {
            @Override
            public void apply(final I r, final List<O> list) {
              final Function1<O, Value<I, O>> _function = new Function1<O, Value<I, O>>() {
                @Override
                public Value<I, O> apply(final O it) {
                  return new Value<I, O>(r, it);
                }
              };
              final List<Value<I, O>> entries = ListExtensions.<O, Value<I, O>>map(list, _function);
              Entries<I, O> _entries = new Entries<I, O>(((Entry<I, O>[])Conversions.unwrapArray(entries, Entry.class)));
              newStream.apply(_entries);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, List<O>>on(stream, _function);
      stream.setOperation("separate");
      StreamExtensions.<I, I, List<O>, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Collect all items from a stream, separated by finishes
   */
  public static <I extends Object, O extends Object> SubStream<I, List<O>> collect(final IStream<I, O> stream) {
    SubStream<I, List<O>> _xblockexpression = null;
    {
      ArrayList<O> _newArrayList = CollectionLiterals.<O>newArrayList();
      final Function2<List<O>, O, List<O>> _function = new Function2<List<O>, O, List<O>>() {
        @Override
        public List<O> apply(final List<O> list, final O it) {
          return StreamExtensions.<O>concat(list, it);
        }
      };
      final SubStream<I, List<O>> s = StreamExtensions.<I, O, List<O>>reduce(stream, _newArrayList, _function);
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
  public static <I extends Object, O extends Object> SubStream<I, String> join(final IStream<I, O> stream, final String separator) {
    final Function2<String, O, String> _function = new Function2<String, O, String>() {
      @Override
      public String apply(final String acc, final O it) {
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
    SubStream<I, String> _reduce = StreamExtensions.<I, O, String>reduce(stream, "", _function);
    final Procedure1<SubStream<I, String>> _function_1 = new Procedure1<SubStream<I, String>>() {
      @Override
      public void apply(final SubStream<I, String> it) {
        stream.setOperation("join");
      }
    };
    return ObjectExtensions.<SubStream<I, String>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Add the value of all the items in the stream until a finish.
   */
  public static <I extends Object, O extends Number> SubStream<I, Double> sum(final IStream<I, O> stream) {
    final Function2<Double, O, Double> _function = new Function2<Double, O, Double>() {
      @Override
      public Double apply(final Double acc, final O it) {
        double _doubleValue = it.doubleValue();
        return Double.valueOf(((acc).doubleValue() + _doubleValue));
      }
    };
    SubStream<I, Double> _reduce = StreamExtensions.<I, O, Double>reduce(stream, Double.valueOf(0D), _function);
    final Procedure1<SubStream<I, Double>> _function_1 = new Procedure1<SubStream<I, Double>>() {
      @Override
      public void apply(final SubStream<I, Double> it) {
        stream.setOperation("sum");
      }
    };
    return ObjectExtensions.<SubStream<I, Double>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Average the items in the stream until a finish.
   */
  public static <I extends Object, O extends Number> SubStream<I, Double> average(final IStream<I, O> stream) {
    SubStream<I, Pair<Integer, O>> _index = StreamExtensions.<I, O>index(stream);
    Pair<Integer, Double> _mappedTo = Pair.<Integer, Double>of(Integer.valueOf(0), Double.valueOf(0D));
    final Function2<Pair<Integer, Double>, Pair<Integer, O>, Pair<Integer, Double>> _function = new Function2<Pair<Integer, Double>, Pair<Integer, O>, Pair<Integer, Double>>() {
      @Override
      public Pair<Integer, Double> apply(final Pair<Integer, Double> acc, final Pair<Integer, O> it) {
        Integer _key = it.getKey();
        Double _value = acc.getValue();
        O _value_1 = it.getValue();
        double _doubleValue = _value_1.doubleValue();
        double _plus = ((_value).doubleValue() + _doubleValue);
        return Pair.<Integer, Double>of(_key, Double.valueOf(_plus));
      }
    };
    SubStream<I, Pair<Integer, Double>> _reduce = StreamExtensions.<I, Pair<Integer, O>, Pair<Integer, Double>>reduce(_index, _mappedTo, _function);
    final Function1<Pair<Integer, Double>, Double> _function_1 = new Function1<Pair<Integer, Double>, Double>() {
      @Override
      public Double apply(final Pair<Integer, Double> it) {
        Double _value = it.getValue();
        Integer _key = it.getKey();
        return Double.valueOf(DoubleExtensions.operator_divide(_value, _key));
      }
    };
    SubStream<I, Double> _map = StreamExtensions.<I, Pair<Integer, Double>, Double>map(_reduce, _function_1);
    final Procedure1<SubStream<I, Double>> _function_2 = new Procedure1<SubStream<I, Double>>() {
      @Override
      public void apply(final SubStream<I, Double> it) {
        stream.setOperation("average");
      }
    };
    return ObjectExtensions.<SubStream<I, Double>>operator_doubleArrow(_map, _function_2);
  }
  
  /**
   * Count the number of items passed in the stream until a finish.
   */
  public static <I extends Object, O extends Object> SubStream<I, Integer> count(final IStream<I, O> stream) {
    final Function2<Integer, O, Integer> _function = new Function2<Integer, O, Integer>() {
      @Override
      public Integer apply(final Integer acc, final O it) {
        return Integer.valueOf(((acc).intValue() + 1));
      }
    };
    SubStream<I, Integer> _reduce = StreamExtensions.<I, O, Integer>reduce(stream, Integer.valueOf(0), _function);
    final Procedure1<SubStream<I, Integer>> _function_1 = new Procedure1<SubStream<I, Integer>>() {
      @Override
      public void apply(final SubStream<I, Integer> it) {
        stream.setOperation("count");
      }
    };
    return ObjectExtensions.<SubStream<I, Integer>>operator_doubleArrow(_reduce, _function_1);
  }
  
  /**
   * Gives the maximum value found on the stream.
   * Values must implement Comparable
   */
  public static <I extends Object, O extends Comparable<O>> SubStream<I, O> max(final IStream<I, O> stream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final Function2<O, O, O> _function = new Function2<O, O, O>() {
        @Override
        public O apply(final O acc, final O it) {
          O _xifexpression = null;
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
      final SubStream<I, O> s = StreamExtensions.<I, O, O>reduce(stream, null, _function);
      stream.setOperation("max");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Gives the minimum value found on the stream.
   * Values must implement Comparable
   */
  public static <I extends Object, O extends Comparable<O>> SubStream<I, O> min(final IStream<I, O> stream) {
    SubStream<I, O> _xblockexpression = null;
    {
      final Function2<O, O, O> _function = new Function2<O, O, O>() {
        @Override
        public O apply(final O acc, final O it) {
          O _xifexpression = null;
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
      final SubStream<I, O> s = StreamExtensions.<I, O, O>reduce(stream, null, _function);
      stream.setOperation("min");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Reduce a stream of values to a single value, and pass a counter in the function.
   * Errors in the stream are suppressed.
   */
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> reduce(final IStream<I, O> stream, final R initial, final Function2<? super R, ? super O, ? extends R> reducerFn) {
    SubStream<I, R> _xblockexpression = null;
    {
      final AtomicReference<R> reduced = new AtomicReference<R>(initial);
      final SubStream<I, R> newStream = new SubStream<I, R>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              try {
                R _get = reduced.get();
                R _apply = reducerFn.apply(_get, $1);
                reduced.set(_apply);
              } finally {
                stream.next();
              }
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                final R result = reduced.getAndSet(initial);
                boolean _notEquals = (!Objects.equal(result, null));
                if (_notEquals) {
                  newStream.push($0, result);
                } else {
                  stream.next();
                }
              } else {
                newStream.finish($0, (($1).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              reduced.set(null);
              stream.skip();
              newStream.error($0, $1);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation((("reduce(initial=" + initial) + ")"));
      StreamExtensions.<I, I, O, R>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> scan(final IStream<I, O> stream, final R initial, final Function2<? super R, ? super O, ? extends R> reducerFn) {
    final Function3<R, I, O, R> _function = new Function3<R, I, O, R>() {
      @Override
      public R apply(final R p, final I r, final O it) {
        return reducerFn.apply(p, it);
      }
    };
    return StreamExtensions.<I, O, R>scan(stream, initial, _function);
  }
  
  public static <I extends Object, O extends Object, R extends Object> SubStream<I, R> scan(final IStream<I, O> stream, final R initial, final Function3<? super R, ? super I, ? super O, ? extends R> reducerFn) {
    SubStream<I, R> _xblockexpression = null;
    {
      final AtomicReference<R> reduced = new AtomicReference<R>(initial);
      final SubStream<I, R> newStream = new SubStream<I, R>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              R _get = reduced.get();
              final R result = reducerFn.apply(_get, $0, $1);
              reduced.set(result);
              boolean _notEquals = (!Objects.equal(result, null));
              if (_notEquals) {
                newStream.push($0, result);
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              reduced.set(initial);
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation((("scan(initial=" + initial) + ")"));
      StreamExtensions.<I, I, O, R>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if all stream values match the test function
   */
  public static <I extends Object, O extends Object> SubStream<I, Boolean> all(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> testFn) {
    SubStream<I, Boolean> _xblockexpression = null;
    {
      final Function2<Boolean, O, Boolean> _function = new Function2<Boolean, O, Boolean>() {
        @Override
        public Boolean apply(final Boolean acc, final O it) {
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
      final SubStream<I, Boolean> s = StreamExtensions.<I, O, Boolean>reduce(stream, Boolean.valueOf(true), _function);
      stream.setOperation("all");
      _xblockexpression = s;
    }
    return _xblockexpression;
  }
  
  /**
   * Streams true if no stream values match the test function
   */
  public static <I extends Object, O extends Object> SubStream<I, Boolean> none(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> testFn) {
    SubStream<I, Boolean> _xblockexpression = null;
    {
      final Function2<Boolean, O, Boolean> _function = new Function2<Boolean, O, Boolean>() {
        @Override
        public Boolean apply(final Boolean acc, final O it) {
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
      final SubStream<I, Boolean> s = StreamExtensions.<I, O, Boolean>reduce(stream, Boolean.valueOf(true), _function);
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
  public static <I extends Object, O extends Object> SubStream<I, Boolean> any(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> testFn) {
    SubStream<I, Boolean> _xblockexpression = null;
    {
      final AtomicBoolean anyMatch = new AtomicBoolean(false);
      final SubStream<I, Boolean> newStream = new SubStream<I, Boolean>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
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
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
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
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("any");
      StreamExtensions.<I, I, O, Boolean>controls(newStream, stream);
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
  public static <I extends Object, O extends Object> SubStream<I, O> first(final IStream<I, O> stream, final Function1<? super O, ? extends Boolean> testFn) {
    final Function2<I, O, Boolean> _function = new Function2<I, O, Boolean>() {
      @Override
      public Boolean apply(final I r, final O it) {
        return testFn.apply(it);
      }
    };
    return StreamExtensions.<I, O>first(stream, _function);
  }
  
  /**
   * Streams the first value that matches the testFn
   * <p>
   * Note that this is not a normal reduction, since no finish is needed to fire a value.
   * The moment testFn gives off true, the value is streamed and the rest of the incoming
   * values are skipped.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> first(final IStream<I, O> stream, final Function2<? super I, ? super O, ? extends Boolean> testFn) {
    SubStream<I, O> _xblockexpression = null;
    {
      final AtomicReference<O> match = new AtomicReference<O>();
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              Boolean _apply = testFn.apply($0, $1);
              if ((_apply).booleanValue()) {
                match.set($1);
                newStream.push($0, $1);
                stream.skip();
              }
              stream.next();
            }
          };
          it.each(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              if ((($1).intValue() == 0)) {
                match.set(null);
              } else {
                newStream.finish($0, (($1).intValue() - 1));
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              newStream.close();
            }
          };
          it.closed(_function_3);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("first");
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Returns an atomic reference which updates as new values come in.
   * This way you can always get the latest value that came from the stream.
   * @return the latest value, self updating. may be null if no value has yet come in.
   */
  public static <I extends Object, O extends Object> AtomicReference<O> latest(final IStream<I, O> stream) {
    AtomicReference<O> _xblockexpression = null;
    {
      final AtomicReference<O> value = new AtomicReference<O>();
      SubStream<I, O> _latest = StreamExtensions.<I, O>latest(stream, value);
      final Procedure1<O> _function = new Procedure1<O>() {
        @Override
        public void apply(final O it) {
        }
      };
      StreamExtensions.<I, O>onEach(_latest, _function);
      _xblockexpression = value;
    }
    return _xblockexpression;
  }
  
  /**
   * Keeps an atomic reference that you pass updated with the latest values
   * that comes from the stream.
   */
  public static <I extends Object, O extends Object> SubStream<I, O> latest(final IStream<I, O> stream, final AtomicReference<O> latestValue) {
    SubStream<I, O> _xblockexpression = null;
    {
      final SubStream<I, O> newStream = new SubStream<I, O>(stream);
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(final StreamResponder<I, O> it) {
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I $0, final O $1) {
              latestValue.set($1);
              newStream.push($0, $1);
            }
          };
          it.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I $0, final Throwable $1) {
              newStream.error($0, $1);
            }
          };
          it.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I $0, final Integer $1) {
              newStream.finish($0, ($1).intValue());
            }
          };
          it.finish(_function_2);
        }
      };
      StreamExtensions.<I, O>on(stream, _function);
      stream.setOperation("latest");
      StreamExtensions.<I, I, O, O>controls(newStream, stream);
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  public static <I extends Object, O extends Object> IStream<I, O> monitor(final IStream<I, O> stream, final StreamMonitor monitor) {
    String _operation = stream.getOperation();
    return StreamExtensions.<I, O>monitor(stream, _operation, monitor);
  }
  
  public static <I extends Object, O extends Object> IStream<I, O> monitor(final IStream<I, O> stream, final String name, final StreamMonitor monitor) {
    IStream<I, O> _xblockexpression = null;
    {
      final StreamStats stats = new StreamStats();
      monitor.add(name, stats);
      _xblockexpression = StreamExtensions.<I, O>monitor(stream, stats);
    }
    return _xblockexpression;
  }
  
  public static <I extends Object, O extends Object> IStream<I, O> monitor(final IStream<I, O> stream, final StreamStats stats) {
    IStream<I, O> _xblockexpression = null;
    {
      final StreamCopySplitter<I, O> splitter = StreamExtensions.<I, O>split(stream);
      IStream<I, O> _stream = splitter.stream();
      final Procedure1<StreamResponder<I, O>> _function = new Procedure1<StreamResponder<I, O>>() {
        @Override
        public void apply(@Extension final StreamResponder<I, O> builder) {
          long _now = StreamExtensions.now();
          stats.setStartTS(Long.valueOf(_now));
          final Procedure2<I, O> _function = new Procedure2<I, O>() {
            @Override
            public void apply(final I from, final O value) {
              final Procedure1<StreamStats> _function = new Procedure1<StreamStats>() {
                @Override
                public void apply(final StreamStats it) {
                  Long _firstEntryTS = it.getFirstEntryTS();
                  boolean _equals = ((_firstEntryTS).longValue() == 0);
                  if (_equals) {
                    long _now = StreamExtensions.now();
                    it.setFirstEntryTS(Long.valueOf(_now));
                  }
                  Long _firstValueTS = it.getFirstValueTS();
                  boolean _equals_1 = ((_firstValueTS).longValue() == 0);
                  if (_equals_1) {
                    long _now_1 = StreamExtensions.now();
                    it.setFirstValueTS(Long.valueOf(_now_1));
                  }
                  long _now_2 = StreamExtensions.now();
                  it.setLastEntryTS(Long.valueOf(_now_2));
                  long _now_3 = StreamExtensions.now();
                  it.setLastValueTS(Long.valueOf(_now_3));
                  it.setLastValue(value);
                  Long _valueCount = it.getValueCount();
                  long _plus = ((_valueCount).longValue() + 1);
                  it.setValueCount(Long.valueOf(_plus));
                }
              };
              ObjectExtensions.<StreamStats>operator_doubleArrow(stats, _function);
              IStream<I, O> _stream = builder.getStream();
              _stream.next();
            }
          };
          builder.each(_function);
          final Procedure2<I, Throwable> _function_1 = new Procedure2<I, Throwable>() {
            @Override
            public void apply(final I from, final Throwable t) {
              final Procedure1<StreamStats> _function = new Procedure1<StreamStats>() {
                @Override
                public void apply(final StreamStats it) {
                  Long _firstEntryTS = it.getFirstEntryTS();
                  boolean _equals = ((_firstEntryTS).longValue() == 0);
                  if (_equals) {
                    long _now = StreamExtensions.now();
                    it.setFirstEntryTS(Long.valueOf(_now));
                  }
                  Long _firstErrorTS = it.getFirstErrorTS();
                  boolean _equals_1 = ((_firstErrorTS).longValue() == 0);
                  if (_equals_1) {
                    long _now_1 = StreamExtensions.now();
                    it.setFirstErrorTS(Long.valueOf(_now_1));
                  }
                  long _now_2 = StreamExtensions.now();
                  it.setLastEntryTS(Long.valueOf(_now_2));
                  long _now_3 = StreamExtensions.now();
                  it.setLastErrorTS(Long.valueOf(_now_3));
                  it.setLastError(t);
                  Long _errorCount = it.getErrorCount();
                  long _plus = ((_errorCount).longValue() + 1);
                  it.setErrorCount(Long.valueOf(_plus));
                }
              };
              ObjectExtensions.<StreamStats>operator_doubleArrow(stats, _function);
              IStream<I, O> _stream = builder.getStream();
              _stream.next();
            }
          };
          builder.error(_function_1);
          final Procedure2<I, Integer> _function_2 = new Procedure2<I, Integer>() {
            @Override
            public void apply(final I from, final Integer t) {
              final Procedure1<StreamStats> _function = new Procedure1<StreamStats>() {
                @Override
                public void apply(final StreamStats it) {
                  Long _firstEntryTS = it.getFirstEntryTS();
                  boolean _equals = ((_firstEntryTS).longValue() == 0);
                  if (_equals) {
                    long _now = StreamExtensions.now();
                    it.setFirstEntryTS(Long.valueOf(_now));
                  }
                  Long _firstFinishTS = it.getFirstFinishTS();
                  boolean _equals_1 = ((_firstFinishTS).longValue() == 0);
                  if (_equals_1) {
                    long _now_1 = StreamExtensions.now();
                    it.setFirstFinishTS(Long.valueOf(_now_1));
                  }
                  long _now_2 = StreamExtensions.now();
                  it.setLastEntryTS(Long.valueOf(_now_2));
                  long _now_3 = StreamExtensions.now();
                  it.setLastFinishTS(Long.valueOf(_now_3));
                  Long _finishCount = it.getFinishCount();
                  long _plus = ((_finishCount).longValue() + 1);
                  it.setFinishCount(Long.valueOf(_plus));
                }
              };
              ObjectExtensions.<StreamStats>operator_doubleArrow(stats, _function);
              IStream<I, O> _stream = builder.getStream();
              _stream.next();
            }
          };
          builder.finish(_function_2);
          final Procedure1<Void> _function_3 = new Procedure1<Void>() {
            @Override
            public void apply(final Void it) {
              final Procedure1<StreamStats> _function = new Procedure1<StreamStats>() {
                @Override
                public void apply(final StreamStats it) {
                  long _now = StreamExtensions.now();
                  it.setLastEntryTS(Long.valueOf(_now));
                  long _now_1 = StreamExtensions.now();
                  it.setCloseTS(Long.valueOf(_now_1));
                }
              };
              ObjectExtensions.<StreamStats>operator_doubleArrow(stats, _function);
            }
          };
          builder.closed(_function_3);
          IStream<I, O> _stream = builder.getStream();
          _stream.next();
        }
      };
      StreamExtensions.<I, O>on(_stream, _function);
      _xblockexpression = splitter.stream();
    }
    return _xblockexpression;
  }
  
  /**
   * Complete a task when the stream finishes or closes,
   * or give an error on the task when the stream gives an error.
   */
  public static void pipe(final IStream<?, ?> stream, final Task task) {
    final Procedure1<StreamResponder<?, ?>> _function = new Procedure1<StreamResponder<?, ?>>() {
      @Override
      public void apply(final StreamResponder<?, ?> it) {
        final Procedure1<Void> _function = new Procedure1<Void>() {
          @Override
          public void apply(final Void it) {
            task.complete();
          }
        };
        it.closed(_function);
        final Procedure2<Object, Integer> _function_1 = new Procedure2<Object, Integer>() {
          @Override
          public void apply(final Object $0, final Integer $1) {
            stream.close();
            task.complete();
          }
        };
        it.finish(_function_1);
        final Procedure2<Object, Throwable> _function_2 = new Procedure2<Object, Throwable>() {
          @Override
          public void apply(final Object $0, final Throwable $1) {
            stream.close();
            task.error($1);
          }
        };
        it.error(_function_2);
        final Procedure2<Object, Object> _function_3 = new Procedure2<Object, Object>() {
          @Override
          public void apply(final Object $0, final Object $1) {
          }
        };
        it.each(_function_3);
      }
    };
    StreamExtensions.on(stream, _function);
    stream.setOperation("pipe");
  }
  
  public static Task toTask(final IStream<?, ?> stream) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      StreamExtensions.pipe(stream, task);
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
  
  private static long now() {
    return System.currentTimeMillis();
  }
}
