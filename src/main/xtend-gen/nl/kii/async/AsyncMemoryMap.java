package nl.kii.async;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nl.kii.async.AsyncMap;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;

/**
 * Converts a normal Map into an AsyncMap
 */
@SuppressWarnings("all")
public class AsyncMemoryMap<K extends Object, V extends Object> implements AsyncMap<K, V> {
  private final Map<K, V> map = new ConcurrentHashMap<K, V>();
  
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
}
