package nl.kii.promise.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
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
      final Future<Integer> future = ExecutorExtensions.<Integer>future(promise);
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
      final Future<Integer> future = ExecutorExtensions.<Integer>future(promise);
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
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(4));
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
    final Promise<Integer> p1 = new Promise<Integer>(Integer.valueOf(3));
    Promise<Promise<Integer>> _promise = new Promise<Promise<Integer>>();
    final IPromise<Promise<Integer>> p2 = PromiseExtensions.<Promise<Integer>>operator_doubleLessThan(_promise, p1);
    final Promise<Integer> flattened = PromiseExtensions.<Integer, Promise<Integer>>flatten(p2);
    StreamAssert.<Integer>assertPromiseEquals(flattened, Integer.valueOf(3));
  }
  
  @Test
  public void testAsync() {
    final Promise<Integer> s = new Promise<Integer>(Integer.valueOf(2));
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
    final Promise<List<Integer>> p = new Promise<List<Integer>>(Collections.<Integer>unmodifiableList(CollectionLiterals.<Integer>newArrayList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))));
    Stream<Integer, Object> _stream = StreamExtensions.<Integer, List<Integer>>stream(p);
    Stream<Double> _sum = StreamExtensions.<Integer>sum(_stream);
    final Procedure1<Double> _function = new Procedure1<Double>() {
      public void apply(final Double it) {
        Assert.assertEquals(6, (it).doubleValue(), 0);
      }
    };
    StreamExtensions.<Double>then(_sum, _function);
  }
  
  @Atomic
  private final AtomicBoolean _allDone = new AtomicBoolean(false);
  
  @Atomic
  private final AtomicBoolean _t2Done = new AtomicBoolean(false);
  
  @Test
  public void testAll() {
    this.setAllDone(Boolean.valueOf(false));
    this.setT2Done(Boolean.valueOf(false));
    final Task t1 = new Task();
    final Task t2 = new Task();
    final Task t3 = new Task();
    final Task a = PromiseExtensions.all(t1, t2, t3);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setT2Done(Boolean.valueOf(true));
      }
    };
    t2.then(_function);
    final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setAllDone(Boolean.valueOf(true));
      }
    };
    a.then(_function_1);
    Boolean _allDone = this.getAllDone();
    Assert.assertFalse((_allDone).booleanValue());
    Boolean _t2Done = this.getT2Done();
    Assert.assertFalse((_t2Done).booleanValue());
    t1.complete();
    Boolean _allDone_1 = this.getAllDone();
    Assert.assertFalse((_allDone_1).booleanValue());
    Boolean _t2Done_1 = this.getT2Done();
    Assert.assertFalse((_t2Done_1).booleanValue());
    t2.complete();
    Boolean _allDone_2 = this.getAllDone();
    Assert.assertFalse((_allDone_2).booleanValue());
    Boolean _t2Done_2 = this.getT2Done();
    Assert.assertTrue((_t2Done_2).booleanValue());
    t3.complete();
    Boolean _allDone_3 = this.getAllDone();
    Assert.assertTrue((_allDone_3).booleanValue());
  }
  
  @Test
  public void testAllOperator() {
    this.setAllDone(Boolean.valueOf(false));
    this.setT2Done(Boolean.valueOf(false));
    final Task t1 = new Task();
    final Task t2 = new Task();
    final Task t3 = new Task();
    Task _and = PromiseExtensions.operator_and(t1, t2);
    final Task a = PromiseExtensions.operator_and(_and, t3);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setT2Done(Boolean.valueOf(true));
      }
    };
    t2.then(_function);
    final Procedure1<Boolean> _function_1 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setAllDone(Boolean.valueOf(true));
      }
    };
    a.then(_function_1);
    Boolean _allDone = this.getAllDone();
    Assert.assertFalse((_allDone).booleanValue());
    Boolean _t2Done = this.getT2Done();
    Assert.assertFalse((_t2Done).booleanValue());
    t1.complete();
    Boolean _allDone_1 = this.getAllDone();
    Assert.assertFalse((_allDone_1).booleanValue());
    Boolean _t2Done_1 = this.getT2Done();
    Assert.assertFalse((_t2Done_1).booleanValue());
    t2.complete();
    Boolean _allDone_2 = this.getAllDone();
    Assert.assertFalse((_allDone_2).booleanValue());
    Boolean _t2Done_2 = this.getT2Done();
    Assert.assertTrue((_t2Done_2).booleanValue());
    t3.complete();
    Boolean _allDone_3 = this.getAllDone();
    Assert.assertTrue((_allDone_3).booleanValue());
  }
  
  @Atomic
  private final AtomicBoolean _anyDone = new AtomicBoolean(false);
  
  @Test
  public void testAny() {
    final Task t1 = new Task();
    final Task t2 = new Task();
    final Task t3 = new Task();
    final Task a = PromiseExtensions.<Boolean, Task>any(t1, t2, t3);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setAnyDone(Boolean.valueOf(true));
      }
    };
    a.then(_function);
    Boolean _anyDone = this.getAnyDone();
    Assert.assertFalse((_anyDone).booleanValue());
    t1.complete();
    Boolean _anyDone_1 = this.getAnyDone();
    Assert.assertTrue((_anyDone_1).booleanValue());
    t2.complete();
    Boolean _anyDone_2 = this.getAnyDone();
    Assert.assertTrue((_anyDone_2).booleanValue());
    t3.complete();
    Boolean _anyDone_3 = this.getAnyDone();
    Assert.assertTrue((_anyDone_3).booleanValue());
  }
  
  @Test
  public void testAnyOperator() {
    final Task t1 = new Task();
    final Task t2 = new Task();
    final Task t3 = new Task();
    Task _or = PromiseExtensions.<Boolean>operator_or(t1, t2);
    final Task a = PromiseExtensions.<Boolean>operator_or(_or, t3);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        TestPromiseExtensions.this.setAnyDone(Boolean.valueOf(true));
      }
    };
    a.then(_function);
    Boolean _anyDone = this.getAnyDone();
    Assert.assertFalse((_anyDone).booleanValue());
    t1.complete();
    Boolean _anyDone_1 = this.getAnyDone();
    Assert.assertTrue((_anyDone_1).booleanValue());
    t2.complete();
    Boolean _anyDone_2 = this.getAnyDone();
    Assert.assertTrue((_anyDone_2).booleanValue());
    t3.complete();
    Boolean _anyDone_3 = this.getAnyDone();
    Assert.assertTrue((_anyDone_3).booleanValue());
  }
  
  private Promise<Integer> power2(final int i) {
    return new Promise<Integer>(Integer.valueOf((i * i)));
  }
  
  private Boolean setAllDone(final Boolean value) {
    return this._allDone.getAndSet(value);
  }
  
  private Boolean getAllDone() {
    return this._allDone.get();
  }
  
  private Boolean setT2Done(final Boolean value) {
    return this._t2Done.getAndSet(value);
  }
  
  private Boolean getT2Done() {
    return this._t2Done.get();
  }
  
  private Boolean setAnyDone(final Boolean value) {
    return this._anyDone.getAndSet(value);
  }
  
  private Boolean getAnyDone() {
    return this._anyDone.get();
  }
}
