package nl.kii.stream.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.kii.stream.Promise;
import nl.kii.stream.PromiseExt;
import nl.kii.stream.StreamAssert;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromiseExt {
  @Test
  public void testFuture() {
    try {
      final Promise<Integer> promise = PromiseExt.<Integer>promise(Integer.class);
      final Future<Integer> future = PromiseExt.<Integer>future(promise);
      PromiseExt.<Integer>operator_doubleLessThan(promise, Integer.valueOf(2));
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
      final Promise<Integer> promise = PromiseExt.<Integer>promise(Integer.class);
      final Future<Integer> future = PromiseExt.<Integer>future(promise);
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
    final Promise<Integer> p = PromiseExt.<Integer>promise(Integer.valueOf(4));
    final Function1<Integer,Integer> _function = new Function1<Integer,Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 10));
      }
    };
    final Promise<Integer> mapped = PromiseExt.<Integer, Integer>map(p, _function);
    StreamAssert.<Integer>assertPromiseEquals(mapped, Integer.valueOf(14));
  }
  
  @Test
  public void testFlatten() {
    final Promise<Integer> p1 = PromiseExt.<Integer>promise(Integer.valueOf(3));
    Promise<Promise<Integer>> _promise = new Promise<Promise<Integer>>();
    final Promise<Promise<Integer>> p2 = PromiseExt.<Promise<Integer>>operator_doubleLessThan(_promise, p1);
    final Promise<Integer> flattened = PromiseExt.<Integer>flatten(p2);
    StreamAssert.<Integer>assertPromiseEquals(flattened, Integer.valueOf(3));
  }
  
  @Test
  public void testAsync() {
    final Promise<Integer> s = PromiseExt.<Integer>promise(Integer.valueOf(2));
    final Function1<Integer,Promise<Integer>> _function = new Function1<Integer,Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromiseExt.this.power2((it).intValue());
      }
    };
    final Promise<Integer> asynced = PromiseExt.<Integer, Integer>async(s, _function);
    StreamAssert.<Integer>assertPromiseEquals(asynced, Integer.valueOf(4));
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExt.<Integer>promise(Integer.valueOf((i * i)));
  }
}
