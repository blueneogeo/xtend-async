package nl.kii.async;

import java.util.Iterator;
import nl.kii.promise.Promise;
import nl.kii.promise.Task;

/**
 * An asynchronous version of a Java Collection.
 * <p>
 * This means that all operations are non-blocking, and instead of returning void and values,
 * they return Tasks and Promises. These can be listened to for the result of the operation,
 * or to catch any thrown exceptions.
 */
@SuppressWarnings("all")
public interface AsyncCollection<T extends Object> {
  public abstract Task add(final T value);
  
  public abstract Task remove(final T value);
  
  public abstract Task clear();
  
  public abstract Promise<Boolean> isEmpty();
  
  public abstract Promise<Integer> size();
  
  public abstract Promise<Iterator<T>> iterator();
}
