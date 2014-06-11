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
  public static <K extends Object, V extends Object> Promise<Pair<K, V>> promisePair(final Pair<Class<K>, Class<V>> type) {
    return new Promise<Pair<K, V>>();
  }
  
  /**
   * Maps a promise of a pair to a new promise, passing the key and value of the incoming
   * promise as listener parameters.
   */
  public static <K1 extends Object, V1 extends Object, V2 extends Object> Promise<V2> map(final Promise<Pair<K1, V1>> promise, final Function2<? super K1, ? super V1, ? extends V2> mappingFn) {
    final Function1<Pair<K1, V1>, V2> _function = new Function1<Pair<K1, V1>, V2>() {
      public V2 apply(final Pair<K1, V1> it) {
        K1 _key = it.getKey();
        V1 _value = it.getValue();
        return mappingFn.apply(_key, _value);
      }
    };
    return PromiseExtensions.<Pair<K1, V1>, V2>map(promise, _function);
  }
  
  /**
   * Same as normal promise resolve, however this time for a pair of a key and a promise.
   */
  public static <K extends Object, V extends Object> Promise<Pair<K, V>> resolvePair(final Promise<Pair<K, Promise<V>>> promise) {
    Promise<Pair<K, V>> _xblockexpression = null;
    {
      final Promise<Pair<K, V>> newPromise = new Promise<Pair<K, V>>(promise);
      final Procedure1<Pair<K, Promise<V>>> _function = new Procedure1<Pair<K, Promise<V>>>() {
        public void apply(final Pair<K, Promise<V>> pair) {
          Promise<V> _value = pair.getValue();
          final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
            public void apply(final Throwable it) {
              newPromise.error(it);
            }
          };
          Promise<V> _onError = _value.onError(_function);
          final Procedure1<V> _function_1 = new Procedure1<V>() {
            public void apply(final V it) {
              K _key = pair.getKey();
              Pair<K, V> _mappedTo = Pair.<K, V>of(_key, it);
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
  public static <K extends Object, V extends Object> void then(final Promise<Pair<K, V>> promise, final Procedure2<? super K, ? super V> listener) {
    final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
      public void apply(final Pair<K, V> it) {
        K _key = it.getKey();
        V _value = it.getValue();
        listener.apply(_key, _value);
      }
    };
    promise.then(_function);
  }
}
