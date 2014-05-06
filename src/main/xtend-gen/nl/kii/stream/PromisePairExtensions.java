package nl.kii.stream;

import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class PromisePairExtensions {
  /**
   * create a promise of a pair
   */
  public static <K extends Object, V extends Object> Promise<Pair<K,V>> promisePair(final Pair<Class<K>,Class<V>> type) {
    return new Promise<Pair<K, V>>();
  }
  
  /**
   * Maps a promise of a pair to a new promise, passing the key and value of the incoming
   * promise as listener parameters.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Promise<V2> map(final Promise<Pair<K1,V1>> promise, final Function2<? super K1,? super V1,? extends V2> mappingFn) {
    final Function1<Pair<K1,V1>,V2> _function = new Function1<Pair<K1,V1>,V2>() {
      public V2 apply(final Pair<K1,V1> it) {
        K1 _key = it.getKey();
        V1 _value = it.getValue();
        return mappingFn.apply(_key, _value);
      }
    };
    return PromiseExtensions.<Pair<K1,V1>, V2>map(promise, _function);
  }
  
  /**
   * Maps a promise of a pair to a new promise, passing the key and value of the incoming
   * promise as listener parameters.
   */
  public static <V1 extends Object, K2 extends Object, V2 extends Object> Promise<Pair<K2,V2>> mapToPair(final Promise<V1> promise, final Function1<? super V1,? extends Pair<K2,V2>> mappingFn) {
    Promise<Pair<K2,V2>> _xblockexpression = null;
    {
      final Promise<Pair<K2,V2>> newPromise = new Promise<Pair<K2, V2>>(promise);
      final Procedure1<V1> _function = new Procedure1<V1>() {
        public void apply(final V1 it) {
          final Pair<K2,V2> pair = mappingFn.apply(it);
          newPromise.set(pair);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Maps a promise of a pair to a new promise, passing the key and value of the incoming
   * promise as listener parameters.
   */
  public static <K1 extends Object, V1 extends Object, K2 extends Object, V2 extends Object> Promise<Pair<K2,V2>> mapToPair(final Promise<Pair<K1,V1>> promise, final Function2<? super K1,? super V1,? extends Pair<K2,V2>> mappingFn) {
    Promise<Pair<K2,V2>> _xblockexpression = null;
    {
      final Promise<Pair<K2,V2>> newPromise = new Promise<Pair<K2, V2>>(promise);
      final Procedure1<Pair<K1,V1>> _function = new Procedure1<Pair<K1,V1>>() {
        public void apply(final Pair<K1,V1> it) {
          K1 _key = it.getKey();
          V1 _value = it.getValue();
          final Pair<K2,V2> pair = mappingFn.apply(_key, _value);
          newPromise.set(pair);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
   * See chain2() for example of how to use.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Promise<V2> mapAsync(final Promise<Pair<K1,V1>> promise, final Function2<? super K1,? super V1,? extends Promise<V2>> promiseFn) {
    Promise<V2> _xblockexpression = null;
    {
      final Promise<V2> newPromise = new Promise<V2>(promise);
      final Procedure1<Pair<K1,V1>> _function = new Procedure1<Pair<K1,V1>>() {
        public void apply(final Pair<K1,V1> it) {
          K1 _key = it.getKey();
          V1 _value = it.getValue();
          Promise<V2> _apply = promiseFn.apply(_key, _value);
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<V2> _onError = _apply.onError(_function);
          final Procedure1<V2> _function_1 = new Procedure1<V2>() {
            public void apply(final V2 it) {
              newPromise.set(it);
            }
          };
          _onError.then(_function_1);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Perform chaining and allows for passing along a value.
   * One of the problems with stream and promise programming is
   * that in closures, you can pass a result along. In promises,
   * you have no state in the lambda so you lose this information.
   * <p>
   * Example with closures:
   * <pre>
   * loadUser(12) [ user |
   *     uploadUser(user) [ result |
   *         showUploadResult(result, user) // user from top closure is referenced
   *     ]
   * ]
   * </pre>
   * This cannot be simulated with normal chaining:
   * <pre>
   * loadUser(12)
   *    .chain [ uploadUser ]
   *    .then [ showUploadResult(it, user) ] // error, no user known here
   * </pre>
   * However with chain2, you can pass along this extra user:
   * <pre>
   * loadUser(12)
   *    .chain2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise
   *    .then2 [ user, result | showUploadResult(result, user) ] // you get back the user
   */
  public static <V1 extends Object, K2 extends Object, V2 extends Object> Promise<Pair<K2,V2>> asyncToPair(final Promise<V1> promise, final Function1<? super V1,? extends Pair<K2,Promise<V2>>> promiseFn) {
    Promise<Pair<K2,V2>> _xblockexpression = null;
    {
      final Promise<Pair<K2,V2>> newPromise = new Promise<Pair<K2, V2>>(promise);
      final Procedure1<V1> _function = new Procedure1<V1>() {
        public void apply(final V1 it) {
          final Pair<K2,Promise<V2>> pair = promiseFn.apply(it);
          Promise<V2> _value = pair.getValue();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<V2> _onError = _value.onError(_function);
          final Procedure1<V2> _function_1 = new Procedure1<V2>() {
            public void apply(final V2 it) {
              K2 _key = pair.getKey();
              Pair<K2,V2> _mappedTo = Pair.<K2, V2>of(_key, it);
              newPromise.set(_mappedTo);
            }
          };
          _onError.then(_function_1);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Version of chain2 that itself receives a pair as input. For multiple chaining:
   * <pre>
   * loadUser(12)
   *    .chain2 [ user | user -> uploadUser ] // pass the user in the result as a pair with the promise
   *    .chain2 [ user, result | user -> showUploadResult(result, user) ] // you get back the user
   *    .then [ user, result | println(result) ]
   */
  public static <K1 extends Object, V1 extends Object, K2 extends Object, V2 extends Object> Promise<Pair<K2,V2>> asyncToPair(final Promise<Pair<K1,V1>> promise, final Function2<? super K1,? super V1,? extends Pair<K2,Promise<V2>>> promiseFn) {
    Promise<Pair<K2,V2>> _xblockexpression = null;
    {
      final Promise<Pair<K2,V2>> newPromise = new Promise<Pair<K2, V2>>(promise);
      final Procedure1<Pair<K1,V1>> _function = new Procedure1<Pair<K1,V1>>() {
        public void apply(final Pair<K1,V1> it) {
          K1 _key = it.getKey();
          V1 _value = it.getValue();
          final Pair<K2,Promise<V2>> pair = promiseFn.apply(_key, _value);
          Promise<V2> _value_1 = pair.getValue();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<V2> _onError = _value_1.onError(_function);
          final Procedure1<V2> _function_1 = new Procedure1<V2>() {
            public void apply(final V2 it) {
              K2 _key = pair.getKey();
              Pair<K2,V2> _mappedTo = Pair.<K2, V2>of(_key, it);
              newPromise.set(_mappedTo);
            }
          };
          _onError.then(_function_1);
        }
      };
      promise.then(_function);
      _xblockexpression = newPromise;
    }
    return _xblockexpression;
  }
  
  /**
   * Responds to a promise pair with a listener that takes the key and value of the promise result pair.
   * See chain2() for example of how to use.
   */
  public static <K extends Object, V extends Object> void then(final Promise<Pair<K,V>> promise, final Procedure2<? super K,? super V> listener) {
    final Procedure1<Pair<K,V>> _function = new Procedure1<Pair<K,V>>() {
      public void apply(final Pair<K,V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    promise.then(_function);
  }
}
