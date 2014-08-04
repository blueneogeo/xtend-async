package nl.kii.promise.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
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
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(Integer.valueOf(4));
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 10));
      }
    };
    final Promise<Integer> mapped = PromiseExtensions.<Integer, Integer>map(p, _function);
    StreamAssert.<Integer>assertPromiseEquals(mapped, Integer.valueOf(14));
  }
  
  @Test
  public void testFlatten() {
    final Promise<Integer> p1 = PromiseExtensions.<Integer>promise(Integer.valueOf(3));
    Promise<Promise<Integer>> _promise = new Promise<Promise<Integer>>();
    final Promise<Promise<Integer>> p2 = PromiseExtensions.<Promise<Integer>>operator_doubleLessThan(_promise, p1);
    final Promise<Integer> flattened = PromiseExtensions.<Integer, Promise<Integer>>flatten(p2);
    StreamAssert.<Integer>assertPromiseEquals(flattened, Integer.valueOf(3));
  }
  
  @Test
  public void testAsync() {
    final Promise<Integer> s = PromiseExtensions.<Integer>promise(Integer.valueOf(2));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      public Promise<Integer> apply(final Integer it) {
        return TestPromiseExtensions.this.power2((it).intValue());
      }
    };
    Promise<Promise<Integer>> _map = PromiseExtensions.<Integer, Promise<Integer>>map(s, _function);
    final Promise<Integer> asynced = PromiseExtensions.<Integer, Promise<Integer>>flatten(_map);
    StreamAssert.<Integer>assertPromiseEquals(asynced, Integer.valueOf(4));
  }
  
  @Test
  public void testListPromiseToStream() {
    final Promise<List<Integer>> p = PromiseExtensions.<List<Integer>>promise(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Stream<Integer> _stream = PromiseExtensions.<Integer, List<Integer>>stream(p);
    Stream<Double> _sum = StreamExtensions.<Integer>sum(_stream);
    final Procedure1<Double> _function = new Procedure1<Double>() {
      public void apply(final Double it) {
        Assert.assertEquals(6, (it).doubleValue(), 0);
      }
    };
    StreamExtensions.<Double>then(_sum, _function);
  }
  
  private Promise<Integer> power2(final int i) {
    return PromiseExtensions.<Integer>promise(Integer.valueOf((i * i)));
  }
}
