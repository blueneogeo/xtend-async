package nl.kii.promise.test;

import com.google.common.base.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.ExecutorExtensions;
import nl.kii.async.annotation.Async;
import nl.kii.async.annotation.Atomic;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.StreamAssert;
import nl.kii.stream.message.Entry;
import nl.kii.util.JUnitExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPromise {
  @Atomic
  private final AtomicInteger _result = new AtomicInteger(0);
  
  @Test
  public void canBeFulfilledBeforeListening() {
    final Promise<Integer> promise = new Promise<Integer>();
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(it);
      }
    };
    promise.then(_function);
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(1), _result_1);
  }
  
  @Test
  public void canBeFulfilledAfterListening() {
    final Promise<Integer> promise = new Promise<Integer>();
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(it);
      }
    };
    promise.then(_function);
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(1), _result_1);
  }
  
  @Test
  public void silentlyFailsWithoutHandler() {
    final Promise<Integer> promise = new Promise<Integer>();
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(Integer.valueOf(((it).intValue() / 0)));
      }
    };
    promise.then(_function);
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result_1);
  }
  
  @Test
  public void canCatchErrorsBeforeListening() {
    final Promise<Integer> promise = new Promise<Integer>();
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    final Procedure1<Throwable> _function = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromise.this.setResult(Integer.valueOf(1));
      }
    };
    IPromise<Integer, Integer> _on = promise.on(Throwable.class, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(Integer.valueOf(((it).intValue() / 0)));
      }
    };
    _on.then(_function_1);
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(1), _result_1);
  }
  
  @Test
  public void canCatchErrorsAfterListening() {
    final Promise<Integer> promise = new Promise<Integer>();
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(Integer.valueOf(((it).intValue() / 0)));
      }
    };
    Task _then = promise.then(_function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromise.this.setResult(Integer.valueOf(1));
      }
    };
    _then.on(Throwable.class, _function_1);
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(1), _result_1);
  }
  
  @Test
  public void canCatchSpecificErrors() {
    final Promise<Integer> promise = new Promise<Integer>();
    Integer _result = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(0), _result);
    promise.set(Integer.valueOf(1));
    final Procedure1<Integer> _function = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        TestPromise.this.setResult(Integer.valueOf(((it).intValue() / 0)));
      }
    };
    Task _then = promise.then(_function);
    final Procedure1<Throwable> _function_1 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        Assert.fail("the error is not a nullpointer exception");
      }
    };
    IPromise<Boolean, Boolean> _on = _then.on(NullPointerException.class, _function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromise.this.setResult(Integer.valueOf(1));
      }
    };
    IPromise<Boolean, Boolean> _on_1 = _on.on(ArithmeticException.class, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        Assert.fail("this may no longer match, the error has already been caught");
      }
    };
    _on_1.on(Throwable.class, _function_3);
    Integer _result_1 = this.getResult();
    JUnitExtensions.<Integer>operator_spaceship(
      Integer.valueOf(1), _result_1);
  }
  
  @Test
  public void testPromiseChaining() {
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(1));
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return new Promise<Integer>(Integer.valueOf(2));
      }
    };
    SubPromise<Integer, Promise<Integer>> _map = PromiseExtensions.<Integer, Integer, Promise<Integer>>map(p, _function);
    final SubPromise<Integer, Integer> p2 = PromiseExtensions.<Integer, Integer, Promise<Integer>>resolve(_map);
    StreamAssert.<Integer>assertPromiseEquals(p2, Integer.valueOf(2));
  }
  
  @Test
  public void testTaskChain() {
    Task _sayHello = this.sayHello();
    final Function1<Boolean, Task> _function = new Function1<Boolean, Task>() {
      @Override
      public Task apply(final Boolean it) {
        return TestPromise.this.sayHello();
      }
    };
    SubPromise<Boolean, Task> _map = PromiseExtensions.<Boolean, Boolean, Task>map(_sayHello, _function);
    SubPromise<Boolean, Boolean> _resolve = PromiseExtensions.<Boolean, Boolean, Task>resolve(_map);
    final Function1<Boolean, Task> _function_1 = new Function1<Boolean, Task>() {
      @Override
      public Task apply(final Boolean it) {
        return TestPromise.this.sayHello();
      }
    };
    SubPromise<Boolean, Task> _map_1 = PromiseExtensions.<Boolean, Boolean, Task>map(_resolve, _function_1);
    SubPromise<Boolean, Boolean> _resolve_1 = PromiseExtensions.<Boolean, Boolean, Task>resolve(_map_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      @Override
      public void apply(final Boolean it) {
        TestPromise.this.sayHello();
      }
    };
    _resolve_1.then(_function_2);
  }
  
  @Test
  public void testLongChain() {
    final AtomicBoolean alwaysDone = new AtomicBoolean();
    final AtomicReference<Throwable> caughtError = new AtomicReference<Throwable>();
    Promise<Integer> _addOne = this.addOne(1);
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_addOne, _function);
    final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_1 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call, _function_1);
    final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_2 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_1, _function_2);
    final Function1<Integer, Promise<Integer>> _function_3 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_3 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_2, _function_3);
    final Function1<Integer, Promise<Integer>> _function_4 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_4 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_3, _function_4);
    final Function1<Integer, Promise<Integer>> _function_5 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_5 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_4, _function_5);
    final Function1<Integer, Promise<Integer>> _function_6 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_6 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_5, _function_6);
    final Function1<Integer, Promise<Integer>> _function_7 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_7 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_6, _function_7);
    final Procedure1<Throwable> _function_8 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        caughtError.set(it);
      }
    };
    IPromise<Integer, Integer> _on = _call_7.on(Throwable.class, _function_8);
    final Procedure1<Entry<?, Integer>> _function_9 = new Procedure1<Entry<?, Integer>>() {
      @Override
      public void apply(final Entry<?, Integer> it) {
        alwaysDone.set(true);
      }
    };
    IPromise<Integer, Integer> _always = PromiseExtensions.<Integer, Integer>always(_on, _function_9);
    StreamAssert.<Integer>assertPromiseEquals(_always, Integer.valueOf(10));
    boolean _get = alwaysDone.get();
    Assert.assertEquals(Boolean.valueOf(true), Boolean.valueOf(_get));
    Throwable _get_1 = caughtError.get();
    Assert.assertNull(_get_1);
  }
  
  @Atomic
  private final AtomicBoolean _alwaysDone = new AtomicBoolean();
  
  @Atomic
  private final AtomicReference<Throwable> _caughtError = new AtomicReference<Throwable>();
  
  @Test
  public void testLongChainWithError() {
    Promise<Integer> _addOne = this.addOne(1);
    final Function1<Integer, Promise<Integer>> _function = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_addOne, _function);
    final Function1<Integer, Promise<Integer>> _function_1 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_1 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call, _function_1);
    final Function1<Integer, Promise<Integer>> _function_2 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_2 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_1, _function_2);
    final Function1<Integer, Promise<Integer>> _function_3 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_3 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_2, _function_3);
    final Function1<Integer, Promise<Integer>> _function_4 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_4 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_3, _function_4);
    final Function1<Integer, Promise<Integer>> _function_5 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_5 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_4, _function_5);
    final Function1<Integer, Promise<Integer>> _function_6 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        try {
          Promise<Integer> _xblockexpression = null;
          {
            boolean _notEquals = (!Objects.equal(it, null));
            if (_notEquals) {
              InputOutput.<Integer>println(it);
              throw new Exception("help!");
            }
            _xblockexpression = TestPromise.this.addOne((it).intValue());
          }
          return _xblockexpression;
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubPromise<Integer, Integer> _call_6 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_5, _function_6);
    final Function1<Integer, Promise<Integer>> _function_7 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_7 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_6, _function_7);
    final Function1<Integer, Promise<Integer>> _function_8 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_8 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_7, _function_8);
    final Function1<Integer, Promise<Integer>> _function_9 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_9 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_8, _function_9);
    final Function1<Integer, Promise<Integer>> _function_10 = new Function1<Integer, Promise<Integer>>() {
      @Override
      public Promise<Integer> apply(final Integer it) {
        return TestPromise.this.addOne((it).intValue());
      }
    };
    SubPromise<Integer, Integer> _call_10 = PromiseExtensions.<Integer, Integer, Integer, Promise<Integer>>call(_call_9, _function_10);
    final Procedure1<Throwable> _function_11 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        InputOutput.<String>println("xxx");
        TestPromise.this.setCaughtError(it);
      }
    };
    IPromise<Integer, Integer> _on = _call_10.on(Throwable.class, _function_11);
    final Procedure1<Integer> _function_12 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        InputOutput.<String>println("hello");
        Assert.fail(("should not get here" + it));
      }
    };
    _on.then(_function_12);
    Throwable _caughtError = this.getCaughtError();
    Assert.assertNotNull(_caughtError);
  }
  
  private final ExecutorService threads = Executors.newCachedThreadPool();
  
  @Async
  public Promise<IPromise<Integer, Integer>> addOne(final int n, final Promise<Integer> promise) {
    final Callable<IPromise<Integer, Integer>> _function = new Callable<IPromise<Integer, Integer>>() {
      @Override
      public IPromise<Integer, Integer> call() throws Exception {
        return PromiseExtensions.<Integer, Integer>operator_doubleLessThan(promise, Integer.valueOf((n + 1)));
      }
    };
    return ExecutorExtensions.<IPromise<Integer, Integer>>promise(this.threads, _function);
  }
  
  @Async
  public Promise<Task> sayHello(final Task task) {
    final Callable<Task> _function = new Callable<Task>() {
      @Override
      public Task call() throws Exception {
        Task _xblockexpression = null;
        {
          InputOutput.<String>println("hello");
          _xblockexpression = task.complete();
        }
        return _xblockexpression;
      }
    };
    return ExecutorExtensions.<Task>promise(this.threads, _function);
  }
  
  @Test
  public void testPromiseErrorChaining() {
    final Promise<Integer> p = new Promise<Integer>(Integer.valueOf(1));
    final Promise<Boolean> p2 = PromiseExtensions.<Boolean>promise(boolean.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() - 1));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf((1 / (it).intValue()));
      }
    };
    SubPromise<Integer, Integer> _map_1 = PromiseExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    final Function1<Integer, Integer> _function_2 = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    SubPromise<Integer, Integer> _map_2 = PromiseExtensions.<Integer, Integer, Integer>map(_map_1, _function_2);
    final Procedure1<Throwable> _function_3 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        PromiseExtensions.<Boolean, Boolean>operator_doubleGreaterThan(Boolean.valueOf(true), p2);
      }
    };
    IPromise<Integer, Integer> _on = _map_2.on(Throwable.class, _function_3);
    final Procedure1<Integer> _function_4 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    _on.then(_function_4);
    StreamAssert.<Boolean>assertPromiseEquals(p2, Boolean.valueOf(true));
  }
  
  @Test
  public void testPromiseChain() {
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() + 1));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      @Override
      public void apply(final Boolean it) {
        InputOutput.<Boolean>println(it);
      }
    };
    Task _then_1 = _then.then(_function_2);
    final Procedure1<Boolean> _function_3 = new Procedure1<Boolean>() {
      @Override
      public void apply(final Boolean it) {
        InputOutput.<Boolean>println(it);
      }
    };
    _then_1.then(_function_3);
    p.set(Integer.valueOf(1));
  }
  
  @Atomic
  private final AtomicBoolean _foundError = new AtomicBoolean();
  
  @Test
  public void testPromiseWithLaterError2() {
    this.setFoundError(Boolean.valueOf(false));
    final Promise<Integer> p = PromiseExtensions.<Integer>promise(int.class);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() / 0));
      }
    };
    SubPromise<Integer, Integer> _map = PromiseExtensions.<Integer, Integer, Integer>map(p, _function);
    final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        Assert.fail("it/0 should not succeed");
      }
    };
    Task _then = _map.then(_function_1);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        TestPromise.this.setFoundError(Boolean.valueOf(true));
      }
    };
    _then.on(Throwable.class, _function_2);
    p.set(Integer.valueOf(1));
    Boolean _foundError = this.getFoundError();
    Assert.assertTrue((_foundError).booleanValue());
  }
  
  private void setResult(final Integer value) {
    this._result.set(value);
  }
  
  private Integer getResult() {
    return this._result.get();
  }
  
  private Integer getAndSetResult(final Integer value) {
    return this._result.getAndSet(value);
  }
  
  private Integer incResult() {
    return this._result.incrementAndGet();
  }
  
  private Integer decResult() {
    return this._result.decrementAndGet();
  }
  
  private Integer incResult(final Integer value) {
    return this._result.addAndGet(value);
  }
  
  private void setAlwaysDone(final Boolean value) {
    this._alwaysDone.set(value);
  }
  
  private Boolean getAlwaysDone() {
    return this._alwaysDone.get();
  }
  
  private Boolean getAndSetAlwaysDone(final Boolean value) {
    return this._alwaysDone.getAndSet(value);
  }
  
  private void setCaughtError(final Throwable value) {
    this._caughtError.set(value);
  }
  
  private Throwable getCaughtError() {
    return this._caughtError.get();
  }
  
  private Throwable getAndSetCaughtError(final Throwable value) {
    return this._caughtError.getAndSet(value);
  }
  
  private void setFoundError(final Boolean value) {
    this._foundError.set(value);
  }
  
  private Boolean getFoundError() {
    return this._foundError.get();
  }
  
  private Boolean getAndSetFoundError(final Boolean value) {
    return this._foundError.getAndSet(value);
  }
  
  public Promise<Integer> addOne(final int n) {
    final Promise<Integer> promise = new Promise<Integer>();
    try {
    	addOne(n,promise);
    } catch(Throwable t) {
    	promise.error(t);
    } finally {
    	return promise;
    }
  }
  
  public Task sayHello() {
    final Task task = new Task();
    try {
    	sayHello(task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
}
