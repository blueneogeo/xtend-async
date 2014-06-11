package nl.kii.stream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import nl.kii.stream.AsyncSubscription;
import nl.kii.stream.Finish;
import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SyncSubscription;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure3;

/**
 * These extensions let you pass state with a stream more easily.
 * <p/>
 * In essence what it allows you to do is to work more easily with streams of pairs.
 * When you can pass a pair, you can pass along some state with the value you are streaming.
 * <p/>
 * For example:
 * <pre>
 * // say we want to stream incoming messages from Vert.x:
 * val stream = Message.stream
 * vertx.eventBus.registerHandler('/test') [ it >> stream ]
 * 
 * // now that we have a message to stream, we'd like to keep
 * // a reference to the message down the stream, so we can reply
 * // to it... with pairs we can.
 * // Given some method processMessageAsync that takes a message and results
 * // in a promise:
 * stream.async [ it -> processMessageAsync(it) ].each [ msg, it | msg.reply(it) ]
 * </pre>
 */
@SuppressWarnings("all")
public class StreamPairExtensions {
  /**
   * create a stream of pairs
   */
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> streamPair(final Pair<Class<K>, Class<V>> type) {
    return new Stream<Pair<K, V>>();
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
  
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> resolvePair(final Stream<Pair<K, Promise<V>>> stream) {
    return StreamPairExtensions.<K, V>resolvePair(stream, 1);
  }
  
  public static <K extends Object, V extends Object> Stream<Pair<K, V>> resolvePair(final Stream<Pair<K, Promise<V>>> stream, final int concurrency) {
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
      final Procedure1<AsyncSubscription<Pair<K, Promise<V>>>> _function_1 = new Procedure1<AsyncSubscription<Pair<K, Promise<V>>>>() {
        public void apply(final AsyncSubscription<Pair<K, Promise<V>>> it) {
          final Procedure1<Pair<K, Promise<V>>> _function = new Procedure1<Pair<K, Promise<V>>>() {
            public void apply(final Pair<K, Promise<V>> result) {
              final K key = result.getKey();
              final Promise<V> promise = result.getValue();
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
          final Procedure1<Finish<Pair<K, Promise<V>>>> _function_2 = new Procedure1<Finish<Pair<K, Promise<V>>>>() {
            public void apply(final Finish<Pair<K, Promise<V>>> it) {
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
      StreamExtensions.<Pair<K, Promise<V>>>onAsync(stream, _function_1);
      stream.next();
      _xblockexpression = newStream;
    }
    return _xblockexpression;
  }
  
  /**
   * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
   */
  public static <K extends Object, V extends Object> void onEach(final Stream<Pair<K, V>> stream, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<SyncSubscription<Pair<K, V>>> _function = new Procedure1<SyncSubscription<Pair<K, V>>>() {
      public void apply(final SyncSubscription<Pair<K, V>> it) {
        final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
          public void apply(final Pair<K, V> it) {
            K _key = it.getKey();
            V _value = it.getValue();
            listener.apply(_key, _value);
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Pair<K, V>>on(stream, _function);
  }
  
  /**
   * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
   * See async2() for example of how to use. This version is controlled: the listener gets passed
   * the =stream and must indicate when it is ready for the next value. It also allows you to skip to
   * the next finish.
   */
  public static <K extends Object, V extends Object> void onEach(final Stream<Pair<K, V>> stream, final Procedure3<? super K, ? super V, ? super Stream<Pair<K, V>>> listener) {
    final Procedure1<SyncSubscription<Pair<K, V>>> _function = new Procedure1<SyncSubscription<Pair<K, V>>>() {
      public void apply(final SyncSubscription<Pair<K, V>> it) {
        final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
          public void apply(final Pair<K, V> it) {
            K _key = it.getKey();
            V _value = it.getValue();
            listener.apply(_key, _value, stream);
          }
        };
        it.each(_function);
      }
    };
    StreamExtensions.<Pair<K, V>>on(stream, _function);
  }
}
