package nl.kii.async;

import nl.kii.promise.Promise;
import nl.kii.promise.Task;

/**
 * An asynchronous version of a Java Collection.
 * <p>
 * This means that all operations are non-blocking, and instead of returning void and values,
 * they return Tasks and Promises. These can be listened to for the result of the operation,
 * or to catch any thrown exceptions.
 * <p>
 * Async collections are especially useful representing networked operations, since it allows
 * for slower operations to not block the code and to have a mechanism to catch exceptions.
 */
public interface AsyncCollection<T extends java.lang.Object> {
  public abstract Task add(final T value);
  
  public abstract Task remove(final T value);
  
  public abstract Task clear();
  
  public abstract /* Promise<Boolean> */Object isEmpty();
  
  public abstract /* Promise<Integer> */Object size();
  
  public abstract /* Promise<Iterator<T>> */Object iterator();
}
