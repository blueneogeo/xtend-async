package nl.kii.async;

import com.google.common.base.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nl.kii.async.AsyncMap;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * Converts a normal Map into an AsyncMap
 */
@SuppressWarnings("all")
public class AsyncMemoryMap<K extends Object, V extends Object> implements AsyncMap<K, V> {
  private final Map<K, V> map;
  
  /**
   * Create using a new ConcurrentHashMap
   */
  public AsyncMemoryMap() {
    this(new ConcurrentHashMap<K, V>());
  }
  
  /**
   * Create wrapping your own map
   */
  public AsyncMemoryMap(final Map<K, V> myMap) {
    this.map = myMap;
  }
  
  public Task put(final K key, final V value) {
    Task _xblockexpression = null;
    {
      this.map.put(key, value);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  public Promise<V> get(final K key) {
    V _get = this.map.get(key);
    return PromiseExtensions.<V>promise(_get);
  }
  
  public Task remove(final K key) {
    Task _xblockexpression = null;
    {
      this.map.remove(key);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  public Promise<Map<K, V>> get(final List<K> keys) {
    final Function1<K, Pair<K, V>> _function = new Function1<K, Pair<K, V>>() {
      public Pair<K, V> apply(final K it) {
        V _get = AsyncMemoryMap.this.map.get(it);
        return Pair.<K, V>of(it, _get);
      }
    };
    List<Pair<K, V>> _map = ListExtensions.<K, Pair<K, V>>map(keys, _function);
    Map<K, V> _map_1 = AsyncMemoryMap.<K, V>toMap(_map);
    return PromiseExtensions.<Map<K, V>>promise(_map_1);
  }
  
  private static <K extends Object, V extends Object> Map<K, V> toMap(final Iterable<Pair<K, V>> pairs) {
    HashMap<K, V> _xblockexpression = null;
    {
      final HashMap<K, V> map = CollectionLiterals.<K, V>newHashMap();
      boolean _notEquals = (!Objects.equal(pairs, null));
      if (_notEquals) {
        final Procedure1<Pair<K, V>> _function = new Procedure1<Pair<K, V>>() {
          public void apply(final Pair<K, V> it) {
            K _key = it.getKey();
            V _value = it.getValue();
            map.put(_key, _value);
          }
        };
        IterableExtensions.<Pair<K, V>>forEach(pairs, _function);
      }
      _xblockexpression = map;
    }
    return _xblockexpression;
  }
}
