package nl.kii.stream;

import nl.kii.stream.Promise;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
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
   * Maps a stream of pairs to a new stream, passing the key and value of the incoming
   * stream as listener parameters.
   */
  public static <V1 extends Object, K2 extends Object, V2 extends Object> Stream<Pair<K2, V2>> mapToPair(final Stream<V1> stream, final Function1<? super V1, ? extends Pair<K2, V2>> mappingFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onNextError is undefined for the type StreamPairExtensions"
      + "\nonEach cannot be resolved");
  }
  
  /**
   * Maps a stream of pairs to a new stream, passing the key and value of the incoming
   * stream as listener parameters.
   */
  public static <K1 extends Object, V1 extends Object, K2 extends Object, V2 extends Object> Stream<Pair<K2, V2>> mapToPair(final Stream<Pair<K1, V1>> stream, final Function2<? super K1, ? super V1, ? extends Pair<K2, V2>> mappingFn) {
    Stream<Pair<K2, V2>> _xblockexpression = null;
    {
      final Stream<Pair<K2, V2>> newStream = new Stream<Pair<K2, V2>>();
      final Procedure1<Pair<K1, V1>> _function = new Procedure1<Pair<K1, V1>>() {
        public void apply(final Pair<K1, V1> it) {
          K1 _key = it.getKey();
          V1 _value = it.getValue();
          final Pair<K2, V2> pair = mappingFn.apply(_key, _value);
          newStream.push(pair);
        }
      };
      StreamExtensions.<Pair<K1, V1>>then(stream, _function);
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
   * Responds to a stream pair with a listener that takes the key and value of the promise result pair.
   * See async2() for example of how to use.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Stream<V2> mapAsync(final Stream<Pair<K1, V1>> stream, final Function2<? super K1, ? super V1, ? extends Promise<V2>> promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onNextValue is undefined for the type StreamPairExtensions"
      + "\nThe method or field key is undefined for the type StreamPairExtensions"
      + "\nThe method or field value is undefined for the type StreamPairExtensions"
      + "\nThe method onNextError is undefined for the type StreamPairExtensions"
      + "\nThe method onNextFinish is undefined for the type StreamPairExtensions"
      + "\nType mismatch: cannot convert from Stream<Pair<K1, V1>> to Queue<Entry<V2>>");
  }
  
  /**
   * Perform async chaining and allows for passing along a value.
   * One of the problems with stream and promise programming is
   * that in closures, you can pass a result along. In promises,
   * you have no state in the lambda so you lose this information.
   * This cannot be simulated with normal chaining:
   * <pre>
   * loadUsers()
   *    .chain [ uploadUser ]
   *    .each [ showUploadResult(it, user) ] // error, no user known here
   * </pre>
   * However with chain2, you can pass along this extra user:
   * <pre>
   * loadUsers()
   *    .async2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise
   *    .then2 [ user, result | showUploadResult(result, user) ] // you get back the user
   */
  public static <V1 extends Object, K2 extends Object, V2 extends Object> Stream<Pair<K2, V2>> mapAsyncToPair(final Stream<V1> stream, final Function1<? super V1, ? extends Pair<K2, Promise<V2>>> promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onNextValue is undefined for the type StreamPairExtensions"
      + "\nThe method onNextError is undefined for the type StreamPairExtensions"
      + "\nThe method onNextFinish is undefined for the type StreamPairExtensions"
      + "\nType mismatch: cannot convert from Stream<V1> to Queue<Entry<Pair<K2, V2>>>");
  }
  
  /**
   * Version of async2 that itself receives a pair as input. For multiple chaining:
   * <pre>
   * loadUsers()
   *    .async2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise
   *    .async2 [ user, result | user -> showUploadResult(result, user) ] // you get back the user
   *    .each [ user, result | println(result) ]
   */
  public static <K1 extends Object, V1 extends Object, K2 extends Object, V2 extends Object> Stream<Pair<K2, V2>> mapAsyncToPair(final Stream<Pair<K1, V1>> stream, final Function2<? super K1, ? super V1, ? extends Pair<K2, Promise<V2>>> promiseFn) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method onNextValue is undefined for the type StreamPairExtensions"
      + "\nThe method or field key is undefined for the type StreamPairExtensions"
      + "\nThe method or field value is undefined for the type StreamPairExtensions"
      + "\nThe method onNextError is undefined for the type StreamPairExtensions"
      + "\nThe method onNextFinish is undefined for the type StreamPairExtensions"
      + "\nType mismatch: cannot convert from Stream<Pair<K1, V1>> to Queue<Entry<Pair<K2, V2>>>");
  }
  
  /**
   * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
   */
  public static <K extends Object, V extends Object> void onEach(final Stream<Pair<K, V>> stream, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    StreamExtensions.<Pair<K, V>>onEach(stream, _function);
  }
  
  /**
   * Responds to a stream pair with a listener that takes the key and value of the stream result pair.
   * See async2() for example of how to use. This version is controlled: the listener gets passed
   * the =stream and must indicate when it is ready for the next value. It also allows you to skip to
   * the next finish.
   */
  public static <K extends Object, V extends Object> void onEach(final Stream<Pair<K, V>> stream, final Procedure3<? super K, ? super V, ? super Stream<Pair<K, V>>> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value, stream);
      }
    };
    StreamExtensions.<Pair<K, V>>onEach(stream, _function);
  }
}
