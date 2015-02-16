package nl.kii.async;

import java.util.Collection;
import java.util.Iterator;
import nl.kii.async.AsyncCollection;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;

/**
 * Converts a normal Collection into an AsyncCollection.
 */
@SuppressWarnings("all")
public class AsyncCollectionWrapper<T extends Object> implements AsyncCollection<T> {
  private final Collection<T> collection;
  
  public AsyncCollectionWrapper(final Collection<T> collection) {
    this.collection = collection;
  }
  
  public Task add(final T value) {
    Task _xblockexpression = null;
    {
      this.collection.add(value);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  public Task remove(final T value) {
    Task _xblockexpression = null;
    {
      this.collection.remove(value);
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  public Task clear() {
    Task _xblockexpression = null;
    {
      this.collection.clear();
      Task _task = new Task();
      _xblockexpression = _task.complete();
    }
    return _xblockexpression;
  }
  
  public Promise<Boolean> isEmpty() {
    boolean _isEmpty = this.collection.isEmpty();
    return PromiseExtensions.<Boolean>promise(Boolean.valueOf(_isEmpty));
  }
  
  public Promise<Integer> size() {
    int _size = this.collection.size();
    return PromiseExtensions.<Integer>promise(Integer.valueOf(_size));
  }
  
  public Promise<Iterator<T>> iterator() {
    Iterator<T> _iterator = this.collection.iterator();
    return PromiseExtensions.<Iterator<T>>promise(_iterator);
  }
}
