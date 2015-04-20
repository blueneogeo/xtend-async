package nl.kii.async;

import com.google.common.base.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import nl.kii.async.AsyncMap;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

/**
 * Converts a normal Map into an AsyncMap
 */
@SuppressWarnings("all")
public class AsyncMapWrapper<K extends Object, V extends Object> implements AsyncMap<K, V> {
  private final Map<K, V> map;
  
  /**
   * Create using a new ConcurrentHashMap
   */
  public AsyncMapWrapper() {
    this(new ConcurrentHashMap<K, V>());
  }
  
  /**
   * Create wrapping your own map
   */
  public AsyncMapWrapper(final Map<K, V> myMap) {
    this.map = myMap;
  }
  
  @Override
  public Task put(final K key, final V value) {
    Task _xblockexpression = null;
    {
      this.map.put(key, value);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  @Override
  public Promise<V> get(final K key) {
    V _get = this.map.get(key);
    return PromiseExtensions.<V>promise(_get);
  }
  
  @Override
  public Task remove(final K key) {
    Task _xblockexpression = null;
    {
      this.map.remove(key);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  @Override
  public Promise<Map<K, V>> get(final List<K> keys) {
    final Function1<K, Pair<K, V>> _function = (K it) -> {
      V _get = this.map.get(it);
      return Pair.<K, V>of(it, _get);
    };
    List<Pair<K, V>> _map = ListExtensions.<K, Pair<K, V>>map(keys, _function);
    Map<K, V> _map_1 = AsyncMapWrapper.<K, V>toMap(_map);
    return PromiseExtensions.<Map<K, V>>promise(_map_1);
  }
  
  private static <K extends Object, V extends Object> Map<K, V> toMap(final Iterable<Pair<K, V>> pairs) {
    HashMap<K, V> _xblockexpression = null;
    {
      final HashMap<K, V> map = CollectionLiterals.<K, V>newHashMap();
      boolean _notEquals = (!Objects.equal(pairs, null));
      if (_notEquals) {
        final Consumer<Pair<K, V>> _function = (Pair<K, V> it) -> {
          K _key = it.getKey();
          V _value = it.getValue();
          map.put(_key, _value);
        };
        pairs.forEach(_function);
      }
      _xblockexpression = map;
    }
    return _xblockexpression;
  }
}
