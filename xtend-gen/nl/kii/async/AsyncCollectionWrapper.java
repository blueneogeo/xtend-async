package nl.kii.async;

import nl.kii.async.AsyncCollection;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;

/**
 * Converts a normal Collection into an AsyncCollection.
 */
public class AsyncCollectionWrapper<T extends java.lang.Object> implements AsyncCollection<T> {
  private final /* Collection<T> */Object collection;
  
  public AsyncCollectionWrapper(final /* Collection<T> */Object collection) {
    this.collection = collection;
  }
  
  public Task add(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nadd cannot be resolved");
  }
  
  public Task remove(final T value) {
    throw new Error("Unresolved compilation problems:"
      + "\nremove cannot be resolved");
  }
  
  public Task clear() {
    throw new Error("Unresolved compilation problems:"
      + "\nclear cannot be resolved");
  }
  
  public /* Promise<Boolean> */Object isEmpty() {
    throw new Error("Unresolved compilation problems:"
      + "\nisEmpty cannot be resolved"
      + "\npromise cannot be resolved");
  }
  
  public /* Promise<Integer> */Object size() {
    throw new Error("Unresolved compilation problems:"
      + "\nsize cannot be resolved"
      + "\npromise cannot be resolved");
  }
  
  public /* Promise<Iterator> */Object iterator() {
    throw new Error("Unresolved compilation problems:"
      + "\niterator cannot be resolved"
      + "\npromise cannot be resolved");
  }
}
