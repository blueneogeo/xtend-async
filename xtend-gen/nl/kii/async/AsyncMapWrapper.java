package nl.kii.async;

import nl.kii.async.AsyncMap;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;

/**
 * Converts a normal Map into an AsyncMap
 */
public class AsyncMapWrapper<K extends java.lang.Object, V extends java.lang.Object> implements AsyncMap<K, V> {
  private final /* Map<K, V> */Object map;
  
  /**
   * Create using a new ConcurrentHashMap
   */
  public AsyncMapWrapper() {
    throw new Error("Unresolved compilation problems:"
      + "\nConcurrentHashMap cannot be resolved.");
  }
  
  /**
   * Create wrapping your own map
   */
  public AsyncMapWrapper(final /* Map<K, V> */Object myMap) {
    this.map = myMap;
  }
  
  public Task put(final K key, final V value) {
    throw new Error("Unresolved compilation problems:"
      + "\nput cannot be resolved");
  }
  
  public Promise<V> get(final K key) {
    throw new Error("Unresolved compilation problems:"
      + "\nget cannot be resolved"
      + "\npromise cannot be resolved");
  }
  
  public Task remove(final K key) {
    throw new Error("Unresolved compilation problems:"
      + "\nremove cannot be resolved");
  }
  
  public /* Promise<Map> */Object get(final /* List<K> */Object keys) {
    throw new Error("Unresolved compilation problems:"
      + "\nmap cannot be resolved"
      + "\n-> cannot be resolved"
      + "\nget cannot be resolved"
      + "\ntoMap cannot be resolved"
      + "\npromise cannot be resolved");
  }
  
  private static <K extends java.lang.Object, V extends java.lang.Object> /* Map<K, V> */Object toMap(final /* Iterable<Pair<K, V>> */Object pairs) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field newHashMap is undefined for the type AsyncMapWrapper"
      + "\nThe method or field key is undefined for the type AsyncMapWrapper"
      + "\nThe method or field value is undefined for the type AsyncMapWrapper"
      + "\n!= cannot be resolved"
      + "\nforEach cannot be resolved"
      + "\nput cannot be resolved");
  }
}
