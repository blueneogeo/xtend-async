package nl.kii.async;

import nl.kii.async.AsyncMap;
import nl.kii.promise.Promise;

@SuppressWarnings("all")
public interface IndexedAsyncMap<V extends Object> extends AsyncMap<String, V> {
  /**
   * Add a value on the default index
   */
  public abstract Promise<String> add(final V value);
  
  /**
   * Add a value on the specified counter
   */
  public abstract Promise<String> add(final V value, final String counter);
  
  /**
   * Generate a new key for the given counter
   */
  public abstract Promise<String> newKey(final String counter);
}
