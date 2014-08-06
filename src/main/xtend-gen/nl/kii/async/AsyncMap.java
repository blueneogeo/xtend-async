package nl.kii.async;

import nl.kii.promise.Promise;
import nl.kii.promise.Task;

@SuppressWarnings("all")
public interface AsyncMap<K extends Object, V extends Object> {
  public abstract Task put(final K key, final V value);
  
  public abstract Promise<V> get(final K key);
  
  public abstract Task remove(final K key);
  
  public abstract Task clear();
  
  public abstract Promise<Boolean> isEmpty();
}
