package nl.kii.promise.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromiseExtensions {
  @Test
  public void testFuture() {
    try {
      final Promise<Integer> promise = PromiseExtensions.<Integer>promise(Integer.class);
      final Future<Integer> future = PromiseExtensions.<Integer>future(promise);
      PromiseExtensions.<Integer>operator_doubleLessThan(promise, Integer.valueOf(2));
      boolean _isDone = future.isDone();
      Assert.assertTrue(_isDone);
      Integer _get = future.get();
      Assert.assertEquals((_get).intValue(), 2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testFutureError() {
    try {
      final Promise<Integer> promise = PromiseExtensions.<Integer>promise(Integer.class);
      final Future<Integer> future = PromiseExtensions.<Integer>future(promise);
      Exception _exception = new Exception();
      promise.error(_exception);
      try {
        future.get();
        Assert.fail("get should throw a exception");
      } catch (final Throwable _t) {
        if (_t instanceof ExecutionException) {
          final ExecutionException e = (ExecutionException)_t;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testMap() {
    throw new Error("Unresolved compilation problems:"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  @Test
  public void testFlatten() {
    throw new Error("Unresolved compilation problems:"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  @Test
  public void testAsync() {
    throw new Error("Unresolved compilation problems:"
      + "\nresolve cannot be resolved"
      + "\nassertPromiseEquals cannot be resolved");
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExtensions.<Integer>promise(Integer.valueOf((i * i)));
  }
}
